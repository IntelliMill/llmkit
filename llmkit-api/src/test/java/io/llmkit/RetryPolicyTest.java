package io.llmkit;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RetryPolicyTest {

  @Test
  void defaults() {
    RetryPolicy rp = RetryPolicy.defaults();
    assertEquals(3, rp.getMaxRetries());
    assertEquals(1000, rp.getInitialDelayMs());
    assertEquals(2.0, rp.getBackoffMultiplier());
    assertEquals(30000, rp.getMaxDelayMs());
  }

  @Test
  void customBuilder() {
    RetryPolicy rp =
        RetryPolicy.builder()
            .maxRetries(5)
            .initialDelayMs(500)
            .backoffMultiplier(1.5)
            .maxDelayMs(10000)
            .build();
    assertEquals(5, rp.getMaxRetries());
    assertEquals(500, rp.getInitialDelayMs());
    assertEquals(1.5, rp.getBackoffMultiplier());
    assertEquals(10000, rp.getMaxDelayMs());
  }
}
