package io.llmkit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Log probability information for a single generated token. */
public final class TokenLogprob {

  private final String token;
  private final Double logprob;
  private final List<TopLogprob> topLogprobs;

  public TokenLogprob(String token, Double logprob, List<TopLogprob> topLogprobs) {
    this.token = token;
    this.logprob = logprob;
    this.topLogprobs =
        topLogprobs != null
            ? Collections.unmodifiableList(new ArrayList<>(topLogprobs))
            : Collections.<TopLogprob>emptyList();
  }

  public String getToken() {
    return token;
  }

  public Double getLogprob() {
    return logprob;
  }

  public List<TopLogprob> getTopLogprobs() {
    return topLogprobs;
  }
}
