package io.llmkit.deepseek;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmClientBuilder;
import org.junit.jupiter.api.Test;

class DeepSeekProviderTest {

  @Test
  void name() {
    assertEquals("deepseek", new DeepSeekProvider().name());
  }

  @Test
  void createBuilderReturnsBuilder() {
    LlmClientBuilder builder = new DeepSeekProvider().createBuilder();
    assertNotNull(builder);
  }

  @Test
  void builderRequiresApiKey() {
    LlmClientBuilder builder = new DeepSeekProvider().createBuilder();
    assertThrows(IllegalStateException.class, () -> builder.build());
  }

  @Test
  void builderWithApiKeySucceeds() {
    LlmClientBuilder builder = new DeepSeekProvider().createBuilder().apiKey("sk-test");
    assertNotNull(builder.build());
  }
}
