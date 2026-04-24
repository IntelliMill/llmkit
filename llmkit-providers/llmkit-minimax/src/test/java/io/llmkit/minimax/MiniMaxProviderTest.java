package io.llmkit.minimax;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmClientBuilder;
import org.junit.jupiter.api.Test;

class MiniMaxProviderTest {

  @Test
  void name() {
    assertEquals("minimax", new MiniMaxProvider().name());
  }

  @Test
  void createBuilderReturnsBuilder() {
    LlmClientBuilder builder = new MiniMaxProvider().createBuilder();
    assertNotNull(builder);
  }

  @Test
  void builderRequiresApiKey() {
    LlmClientBuilder builder = new MiniMaxProvider().createBuilder();
    assertThrows(IllegalStateException.class, () -> builder.build());
  }

  @Test
  void builderWithApiKeySucceeds() {
    LlmClientBuilder builder = new MiniMaxProvider().createBuilder().apiKey("test-key");
    assertNotNull(builder.build());
  }
}
