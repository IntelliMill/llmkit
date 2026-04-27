package io.llmkit.openai.integration;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmClient;
import io.llmkit.LlmKit;
import io.llmkit.model.ChatMessage;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import io.llmkit.model.ChoiceLogprobs;
import io.llmkit.model.TokenLogprob;
import io.llmkit.model.TopLogprob;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class OpenAiNewParamsIntegrationTest {

  private static String apiKey;

  @BeforeAll
  static void checkApiKey() {
    apiKey = System.getProperty("openai.api.key");
    if (apiKey == null || apiKey.isEmpty()) {
      apiKey = System.getenv("OPENAI_API_KEY");
    }
    org.junit.jupiter.api.Assumptions.assumeTrue(
        apiKey != null && !apiKey.isEmpty(), "OpenAI API key not set");
  }

  @Test
  void topPParameterAccepted() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("gpt-4o-mini")
                  .addMessage(ChatMessage.user("Say hello"))
                  .topP(0.5)
                  .build());
      assertNotNull(response.content());
      assertTrue(response.content().length() > 0);
    } finally {
      client.close();
    }
  }

  @Test
  void seedParameterAccepted() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("gpt-4o-mini")
                  .addMessage(ChatMessage.user("Say hello"))
                  .seed(42)
                  .build());
      assertNotNull(response.content());
    } finally {
      client.close();
    }
  }

  @Test
  void seedProducesDeterministicResults() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ChatRequest request =
          ChatRequest.builder()
              .model("gpt-4o-mini")
              .addMessage(ChatMessage.user("Reply with exactly one word: APPLE"))
              .temperature(0.0)
              .seed(12345)
              .build();

      ChatResponse r1 = client.chat(request);
      ChatResponse r2 = client.chat(request);

      assertNotNull(r1.content());
      assertNotNull(r2.content());
      assertEquals(r1.content(), r2.content(), "Seed should produce deterministic results");
    } finally {
      client.close();
    }
  }

  @Test
  void stopSequenceWorks() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("gpt-4o-mini")
                  .addMessage(
                      ChatMessage.user(
                          "Count from 1 to 100, separated by commas. Do not use line breaks."))
                  .stop(java.util.Arrays.asList("11", "STOP"))
                  .temperature(0.0)
                  .build());
      assertNotNull(response.content());
      String content = response.content();
      // Should be truncated before "11" or "STOP" appears
      assertFalse(content.contains("11"), "Content should stop before 11: " + content);
      assertEquals("stop", response.getChoices().get(0).getFinishReason());
    } finally {
      client.close();
    }
  }

  @Test
  void logprobsReturnsTokenData() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("gpt-4o-mini")
                  .addMessage(ChatMessage.user("Say exactly: hello world"))
                  .temperature(0.0)
                  .logprobs(true)
                  .topLogprobs(3)
                  .build());

      assertNotNull(response.content());
      ChoiceLogprobs lp = response.getChoices().get(0).getLogprobs();
      assertNotNull(lp, "Logprobs should be present when requested");
      assertFalse(lp.getContent().isEmpty(), "Logprobs content should not be empty");

      // Verify each token has required fields
      for (TokenLogprob tl : lp.getContent()) {
        assertNotNull(tl.getToken(), "Token string should not be null");
        assertNotNull(tl.getLogprob(), "Logprob should not be null");
        assertTrue(tl.getLogprob() <= 0.0, "Logprob should be <= 0");
        assertNotNull(tl.getTopLogprobs(), "Top logprobs list should not be null");
      }

      // With topLogprobs=3, each token should have up to 3 top entries
      for (TokenLogprob tl : lp.getContent()) {
        assertTrue(
            tl.getTopLogprobs().size() <= 3,
            "Top logprobs should have at most 3 entries, got: " + tl.getTopLogprobs().size());
        assertTrue(tl.getTopLogprobs().size() > 0, "Top logprobs should have at least 1 entry");
        for (TopLogprob top : tl.getTopLogprobs()) {
          assertNotNull(top.getToken());
          assertNotNull(top.getLogprob());
        }
      }
    } finally {
      client.close();
    }
  }

  @Test
  void responseFormatJsonMode() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("gpt-4o-mini")
                  .addMessage(
                      ChatMessage.system("You must respond with valid JSON only. No other text."))
                  .addMessage(
                      ChatMessage.user(
                          "Return a JSON object with a single key \"color\" whose value is \"red\"."))
                  .responseFormat(ChatRequest.jsonResponseFormat())
                  .temperature(0.0)
                  .build());

      assertNotNull(response.content());
      String content = response.content().trim();
      // Should be valid JSON
      assertTrue(
          content.startsWith("{") && content.endsWith("}"),
          "Response should be a JSON object: " + content);
      assertTrue(
          content.contains("\"color\"") && content.contains("\"red\""),
          "JSON should contain color=red: " + content);
    } finally {
      client.close();
    }
  }

  @Test
  void responseFormatStructuredOutput() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("gpt-4o-mini")
                  .addMessage(ChatMessage.user("What is the capital of Japan?"))
                  .responseFormat(
                      ChatRequest.schemaResponseFormat(
                          "geo_info",
                          "{\"type\":\"object\",\"properties\":{\"capital\":{\"type\":\"string\"},\"country\":{\"type\":\"string\"}},\"required\":[\"capital\",\"country\"],\"additionalProperties\":false}"))
                  .temperature(0.0)
                  .build());

      assertNotNull(response.content());
      String content = response.content().trim();
      assertTrue(
          content.startsWith("{") && content.endsWith("}"),
          "Response should be a JSON object: " + content);
      assertTrue(
          content.contains("\"capital\""), "JSON should contain 'capital' field: " + content);
      assertTrue(
          content.contains("\"country\""), "JSON should contain 'country' field: " + content);
    } finally {
      client.close();
    }
  }
}
