package io.llmkit.anthropic;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmClientBuilder;
import org.junit.jupiter.api.Test;

class AnthropicProviderTest {

  @Test
  void name() {
    assertEquals("anthropic", new AnthropicProvider().name());
  }

  @Test
  void createBuilderReturnsBuilder() {
    LlmClientBuilder builder = new AnthropicProvider().createBuilder();
    assertNotNull(builder);
  }

  @Test
  void builderRequiresApiKey() {
    LlmClientBuilder builder = new AnthropicProvider().createBuilder();
    assertThrows(IllegalStateException.class, () -> builder.build());
  }

  @Test
  void builderWithApiKeySucceeds() {
    LlmClientBuilder builder = new AnthropicProvider().createBuilder().apiKey("sk-ant-test");
    assertNotNull(builder.build());
  }
}
