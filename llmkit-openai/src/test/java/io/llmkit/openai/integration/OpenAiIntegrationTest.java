package io.llmkit.openai.integration;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmClient;
import io.llmkit.LlmKit;
import io.llmkit.StreamListener;
import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatMessage;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import io.llmkit.model.ToolDefinition;
import io.llmkit.model.Usage;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class OpenAiIntegrationTest {

  private static String apiKey;

  @BeforeAll
  static void checkApiKey() {
    apiKey = System.getProperty("openai.api.key");
    if (apiKey == null || apiKey.isEmpty()) {
      apiKey = System.getenv("OPENAI_API_KEY");
    }
    assumeTrue(apiKey != null && !apiKey.isEmpty(), "OpenAI API key not set");
  }

  private static void assumeTrue(boolean condition, String message) {
    org.junit.jupiter.api.Assumptions.assumeTrue(condition, message);
  }

  @Test
  void basicChatReturnsContent() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("gpt-4o-mini")
                  .addMessage(ChatMessage.user("Say exactly: PONG"))
                  .build());
      assertNotNull(response);
      assertNotNull(response.content());
      assertTrue(
          response.content().toLowerCase().contains("pong"),
          "Expected 'pong' in: " + response.content());
      assertNotNull(response.getModel());
      assertFalse(response.getChoices().isEmpty());
    } finally {
      client.close();
    }
  }

  @Test
  void chatWithMessages() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ChatRequest request =
          ChatRequest.builder()
              .model("gpt-4o-mini")
              .addMessage(
                  ChatMessage.system("You are a helpful assistant. Reply in one word only."))
              .addMessage(ChatMessage.user("What is the capital of France?"))
              .build();

      ChatResponse response = client.chat(request);
      assertNotNull(response.content());
      String content = response.content().toLowerCase();
      assertTrue(content.contains("paris"), "Expected 'paris' in: " + response.content());
    } finally {
      client.close();
    }
  }

  @Test
  void responseIncludesUsage() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("gpt-4o-mini")
                  .addMessage(ChatMessage.user("Hi"))
                  .build());
      Usage usage = response.getUsage();
      assertNotNull(usage, "Usage should be present");
      assertTrue(usage.getPromptTokens() > 0, "prompt tokens > 0");
      assertTrue(usage.getCompletionTokens() > 0, "completion tokens > 0");
      assertTrue(usage.getTotalTokens() > 0, "total tokens > 0");
    } finally {
      client.close();
    }
  }

  @Test
  void responseIncludesMetadata() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("gpt-4o-mini")
                  .addMessage(ChatMessage.user("Hi"))
                  .build());
      assertNotNull(response.getId());
      assertTrue(response.getId().startsWith("chatcmpl-"), "ID should start with chatcmpl-");
      assertNotNull(response.getModel());
      assertEquals("stop", response.getChoices().get(0).getFinishReason());
    } finally {
      client.close();
    }
  }

  @Test
  void streamingReturnsContent() throws Exception {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      CountDownLatch latch = new CountDownLatch(1);
      StringBuilder collected = new StringBuilder();
      AtomicReference<Throwable> error = new AtomicReference<>();

      ChatRequest request =
          ChatRequest.builder()
              .model("gpt-4o-mini")
              .addMessage(ChatMessage.user("Count from 1 to 5"))
              .build();

      client.chatStream(
          request,
          new StreamListener() {
            @Override
            public void onChunk(ChatChunk chunk) {
              String delta = chunk.delta();
              if (delta != null) {
                collected.append(delta);
              }
            }

            @Override
            public void onComplete(ChatResponse resp) {
              latch.countDown();
            }

            @Override
            public void onError(Throwable t) {
              error.set(t);
              latch.countDown();
            }
          });

      assertTrue(latch.await(30, TimeUnit.SECONDS), "Stream should complete within 30s");
      assertNull(
          error.get(),
          "No error expected" + (error.get() != null ? ": " + error.get().getMessage() : ""));
      assertTrue(collected.length() > 0, "Should have received streaming content");
    } finally {
      client.close();
    }
  }

  @Test
  void toolCallingWorks() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      ToolDefinition weatherTool =
          ToolDefinition.builder()
              .name("get_weather")
              .description("Get the current weather in a given location")
              .parameters(
                  "{\"type\":\"object\",\"properties\":{\"location\":{\"type\":\"string\",\"description\":\"City name\"}},\"required\":[\"location\"]}")
              .build();

      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("gpt-4o-mini")
                  .addMessage(ChatMessage.user("What's the weather in Tokyo?"))
                  .tools(Collections.singletonList(weatherTool))
                  .build());

      assertNotNull(response);
      assertFalse(response.getChoices().isEmpty());
      ChatMessage msg = response.getChoices().get(0).getMessage();
      assertFalse(msg.getToolCalls().isEmpty(), "Model should request tool call");

      io.llmkit.model.ToolCall tc = msg.getToolCalls().get(0);
      assertEquals("get_weather", tc.getName());
      assertNotNull(tc.getId());
      assertTrue(
          tc.getArguments().toLowerCase().contains("tokyo"),
          "Args should contain tokyo: " + tc.getArguments());
    } finally {
      client.close();
    }
  }

  @Test
  void customBaseUrlWorksWithOpenAIFormat() {
    // Test with OpenAI base URL explicitly set
    LlmClient client =
        LlmKit.builder("openai")
            .apiKey(apiKey)
            .baseUrl("https://api.openai.com")
            .model("gpt-4o-mini")
            .build();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("gpt-4o-mini")
                  .addMessage(ChatMessage.user("Say OK"))
                  .build());
      assertNotNull(response.content());
    } finally {
      client.close();
    }
  }

  @Test
  void oneShotChat() {
    LlmClient client = LlmKit.builder("openai").apiKey(apiKey).model("gpt-4o-mini").build();
    try {
      String answer = client.chat("Reply with just the word YES");
      assertNotNull(answer);
      assertTrue(answer.toUpperCase().contains("YES"), "Expected YES in: " + answer);
    } finally {
      client.close();
    }
  }
}
