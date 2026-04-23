package io.llmkit.openai;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmClientBuilder;
import org.junit.jupiter.api.Test;

class OpenAiProviderTest {

  @Test
  void name() {
    assertEquals("openai", new OpenAiProvider().name());
  }

  @Test
  void createBuilderReturnsBuilder() {
    LlmClientBuilder builder = new OpenAiProvider().createBuilder();
    assertNotNull(builder);
  }

  @Test
  void builderRequiresApiKey() {
    LlmClientBuilder builder = new OpenAiProvider().createBuilder();
    assertThrows(IllegalStateException.class, () -> builder.build());
  }

  @Test
  void builderWithApiKeySucceeds() {
    LlmClientBuilder builder = new OpenAiProvider().createBuilder().apiKey("sk-test");
    assertNotNull(builder.build());
  }
}
