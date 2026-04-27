package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class TokenLogprobTest {

  @Test
  void basicConstruction() {
    TopLogprob t1 = new TopLogprob("Hello", -0.12);
    TopLogprob t2 = new TopLogprob("hello", -1.56);
    TokenLogprob tl = new TokenLogprob("Hello", -0.12, Arrays.asList(t1, t2));
    assertEquals("Hello", tl.getToken());
    assertEquals(-0.12, tl.getLogprob());
    assertEquals(2, tl.getTopLogprobs().size());
  }

  @Test
  void nullTopLogprobsBecomesEmptyList() {
    TokenLogprob tl = new TokenLogprob("x", -0.1, null);
    assertTrue(tl.getTopLogprobs().isEmpty());
  }

  @Test
  void topLogprobsIsImmutable() {
    TokenLogprob tl =
        new TokenLogprob("x", -0.1, Collections.singletonList(new TopLogprob("y", -0.2)));
    assertThrows(UnsupportedOperationException.class, () -> tl.getTopLogprobs().add(null));
  }
}
