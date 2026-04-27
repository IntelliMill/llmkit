package io.llmkit.model;

/** A single candidate in the top-N log probabilities for a token position. */
public final class TopLogprob {

  private final String token;
  private final Double logprob;

  public TopLogprob(String token, Double logprob) {
    this.token = token;
    this.logprob = logprob;
  }

  public String getToken() {
    return token;
  }

  public Double getLogprob() {
    return logprob;
  }
}
