package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TopLogprobTest {

  @Test
  void basicConstruction() {
    TopLogprob t = new TopLogprob("Hello", -0.1234);
    assertEquals("Hello", t.getToken());
    assertEquals(-0.1234, t.getLogprob());
  }
}
