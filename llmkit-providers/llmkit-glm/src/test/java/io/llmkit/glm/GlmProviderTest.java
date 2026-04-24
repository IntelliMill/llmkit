package io.llmkit.glm;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmClientBuilder;
import org.junit.jupiter.api.Test;

class GlmProviderTest {

  @Test
  void name() {
    assertEquals("glm", new GlmProvider().name());
  }

  @Test
  void createBuilderReturnsBuilder() {
    LlmClientBuilder builder = new GlmProvider().createBuilder();
    assertNotNull(builder);
  }

  @Test
  void builderRequiresApiKey() {
    LlmClientBuilder builder = new GlmProvider().createBuilder();
    assertThrows(IllegalStateException.class, () -> builder.build());
  }

  @Test
  void builderWithApiKeySucceeds() {
    LlmClientBuilder builder = new GlmProvider().createBuilder().apiKey("test-key");
    assertNotNull(builder.build());
  }
}
