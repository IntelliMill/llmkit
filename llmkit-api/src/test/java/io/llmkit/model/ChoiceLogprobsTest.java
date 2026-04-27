package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ChoiceLogprobsTest {

  @Test
  void basicConstruction() {
    TokenLogprob t1 = new TokenLogprob("Hello", -0.12, null);
    TokenLogprob t2 = new TokenLogprob(" world", -0.05, null);
    ChoiceLogprobs lp = new ChoiceLogprobs(Arrays.asList(t1, t2));
    assertEquals(2, lp.getContent().size());
    assertEquals("Hello", lp.getContent().get(0).getToken());
    assertEquals(-0.12, lp.getContent().get(0).getLogprob());
  }

  @Test
  void nullContentBecomesEmptyList() {
    ChoiceLogprobs lp = new ChoiceLogprobs(null);
    assertTrue(lp.getContent().isEmpty());
  }

  @Test
  void contentIsImmutable() {
    ChoiceLogprobs lp =
        new ChoiceLogprobs(
            Collections.singletonList(new TokenLogprob("x", -0.1, Collections.emptyList())));
    assertThrows(UnsupportedOperationException.class, () -> lp.getContent().add(null));
  }
}
