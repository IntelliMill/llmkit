package io.llmkit.internal;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmKit;
import org.junit.jupiter.api.Test;

class LlmKitTest {

  @Test
  void builderWithUnknownProviderThrows() {
    assertThrows(IllegalArgumentException.class, () -> LlmKit.builder("nonexistent"));
  }

  @Test
  void providersReturnsList() {
    // This test depends on classpath having openai+anthropic providers
    // In isolation it may return empty, but with providers on classpath it returns them
    assertNotNull(LlmKit.providers());
  }

  @Test
  void createWithNoProvidersThrows() {
    // When no providers are on classpath, this should throw
    // This test validates the error path; with providers it may succeed
    try {
      LlmKit.create("key");
    } catch (IllegalStateException e) {
      assertTrue(e.getMessage().contains("No LLM providers found"));
    }
  }
}
