package io.llmkit;

/** Configuration for retry behavior on transient failures. */
public final class RetryPolicy {

  private final int maxRetries;
  private final long initialDelayMs;
  private final double backoffMultiplier;
  private final int maxDelayMs;

  private RetryPolicy(Builder builder) {
    this.maxRetries = builder.maxRetries;
    this.initialDelayMs = builder.initialDelayMs;
    this.backoffMultiplier = builder.backoffMultiplier;
    this.maxDelayMs = builder.maxDelayMs;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public long getInitialDelayMs() {
    return initialDelayMs;
  }

  public double getBackoffMultiplier() {
    return backoffMultiplier;
  }

  public int getMaxDelayMs() {
    return maxDelayMs;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Default retry policy: 3 retries, 1s initial delay, 2x backoff. */
  public static RetryPolicy defaults() {
    return builder()
        .maxRetries(3)
        .initialDelayMs(1000)
        .backoffMultiplier(2.0)
        .maxDelayMs(30000)
        .build();
  }

  public static final class Builder {
    private int maxRetries = 3;
    private long initialDelayMs = 1000;
    private double backoffMultiplier = 2.0;
    private int maxDelayMs = 30000;

    private Builder() {}

    public Builder maxRetries(int maxRetries) {
      this.maxRetries = maxRetries;
      return this;
    }

    public Builder initialDelayMs(long initialDelayMs) {
      this.initialDelayMs = initialDelayMs;
      return this;
    }

    public Builder backoffMultiplier(double backoffMultiplier) {
      this.backoffMultiplier = backoffMultiplier;
      return this;
    }

    public Builder maxDelayMs(int maxDelayMs) {
      this.maxDelayMs = maxDelayMs;
      return this;
    }

    public RetryPolicy build() {
      return new RetryPolicy(this);
    }
  }
}
