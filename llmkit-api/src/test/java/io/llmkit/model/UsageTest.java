package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UsageTest {

  @Test
  void allFields() {
    Usage u = new Usage(10, 20, 30);
    assertEquals(10, u.getPromptTokens());
    assertEquals(20, u.getCompletionTokens());
    assertEquals(30, u.getTotalTokens());
  }

  @Test
  void zeroUsage() {
    Usage u = new Usage(0, 0, 0);
    assertEquals(0, u.getPromptTokens());
    assertEquals(0, u.getCompletionTokens());
    assertEquals(0, u.getTotalTokens());
  }
}
