package io.llmkit.anthropic.integration;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmClient;
import io.llmkit.LlmKit;
import io.llmkit.Providers;
import io.llmkit.StreamListener;
import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatMessage;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import io.llmkit.model.Usage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
class AnthropicIntegrationTest {

  private static String apiKey;
  private static String baseUrl;

  @BeforeAll
  static void checkApiKey() {
    apiKey = System.getProperty("anthropic.api.key");
    if (apiKey == null || apiKey.isEmpty()) {
      apiKey = System.getenv("ANTHROPIC_API_KEY");
    }
    assumeTrue(apiKey != null && !apiKey.isEmpty(), "Anthropic API key not set");

    baseUrl = System.getProperty("anthropic.base.url");
    if (baseUrl == null || baseUrl.isEmpty()) {
      baseUrl = System.getenv("ANTHROPIC_BASE_URL");
    }
  }

  private static void assumeTrue(boolean condition, String message) {
    org.junit.jupiter.api.Assumptions.assumeTrue(condition, message);
  }

  private LlmClient createClient() {
    io.llmkit.LlmClientBuilder builder =
        LlmKit.builder(Providers.ANTHROPIC).apiKey(apiKey).model("claude-sonnet-4-20250514");
    if (baseUrl != null && !baseUrl.isEmpty()) {
      builder.baseUrl(baseUrl);
    }
    return builder.build();
  }

  @Test
  void basicChatReturnsContent() {
    LlmClient client = createClient();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("claude-sonnet-4-20250514")
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
  void chatWithSystemMessage() {
    LlmClient client = createClient();
    try {
      ChatRequest request =
          ChatRequest.builder()
              .model("claude-sonnet-4-20250514")
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
    LlmClient client = createClient();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("claude-sonnet-4-20250514")
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
    LlmClient client = createClient();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("claude-sonnet-4-20250514")
                  .addMessage(ChatMessage.user("Hi"))
                  .build());
      assertNotNull(response.getId(), "ID should be present");
      assertNotNull(response.getModel(), "Model should be present");
      assertEquals("end_turn", response.getChoices().get(0).getFinishReason());
    } finally {
      client.close();
    }
  }

  @Test
  void streamingReturnsContent() throws Exception {
    LlmClient client = createClient();
    try {
      CountDownLatch latch = new CountDownLatch(1);
      StringBuilder collected = new StringBuilder();
      AtomicReference<Throwable> error = new AtomicReference<>();

      ChatRequest request =
          ChatRequest.builder()
              .model("claude-sonnet-4-20250514")
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
  void oneShotChat() {
    LlmClient client = createClient();
    try {
      String answer = client.chat("Reply with just the word YES");
      assertNotNull(answer);
      assertTrue(answer.toUpperCase().contains("YES"), "Expected YES in: " + answer);
    } finally {
      client.close();
    }
  }

  @Test
  void customBaseUrlWorks() {
    // Verify that custom base URL is being used
    LlmClient client = createClient();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("claude-sonnet-4-20250514")
                  .addMessage(ChatMessage.user("Say OK"))
                  .build());
      assertNotNull(response.content());
    } finally {
      client.close();
    }
  }

  @Test
  void multiTurnConversation() {
    LlmClient client = createClient();
    try {
      ChatResponse response =
          client.chat(
              ChatRequest.builder()
                  .model("claude-sonnet-4-20250514")
                  .addMessage(ChatMessage.user("My name is TestUser. Remember it."))
                  .addMessage(ChatMessage.assistant("Got it, TestUser!"))
                  .addMessage(ChatMessage.user("What is my name?"))
                  .build());
      assertNotNull(response.content());
      assertTrue(
          response.content().contains("TestUser"), "Expected 'TestUser' in: " + response.content());
    } finally {
      client.close();
    }
  }
}
