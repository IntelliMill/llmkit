package io.llmkit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProvidersTest {

  @Test
  void constantValues() {
    assertEquals("openai", Providers.OPENAI);
    assertEquals("anthropic", Providers.ANTHROPIC);
  }
}
