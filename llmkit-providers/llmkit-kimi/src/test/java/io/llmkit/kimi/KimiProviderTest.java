package io.llmkit.kimi;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmClientBuilder;
import org.junit.jupiter.api.Test;

class KimiProviderTest {

  @Test
  void name() {
    assertEquals("kimi", new KimiProvider().name());
  }

  @Test
  void createBuilderReturnsBuilder() {
    LlmClientBuilder builder = new KimiProvider().createBuilder();
    assertNotNull(builder);
  }

  @Test
  void builderRequiresApiKey() {
    LlmClientBuilder builder = new KimiProvider().createBuilder();
    assertThrows(IllegalStateException.class, () -> builder.build());
  }

  @Test
  void builderWithApiKeySucceeds() {
    LlmClientBuilder builder = new KimiProvider().createBuilder().apiKey("sk-test");
    assertNotNull(builder.build());
  }
}
