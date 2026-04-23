package io.llmkit.internal;

import io.llmkit.LlmClient;
import io.llmkit.LlmClientBuilder;
import io.llmkit.RetryPolicy;
import java.time.Duration;

/** Base builder implementation with common configuration. */
public abstract class AbstractLlmClientBuilder<T extends AbstractLlmClientBuilder<T>>
    implements LlmClientBuilder {

  protected String apiKey;
  protected String model;
  protected String baseUrl;
  protected int connectTimeoutMs = 10000;
  protected int readTimeoutMs = 60000;
  protected RetryPolicy retryPolicy;

  protected abstract T self();

  protected abstract LlmClient doBuild();

  @Override
  public LlmClientBuilder apiKey(String apiKey) {
    this.apiKey = apiKey;
    return self();
  }

  @Override
  public LlmClientBuilder model(String model) {
    this.model = model;
    return self();
  }

  @Override
  public LlmClientBuilder baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return self();
  }

  @Override
  public LlmClientBuilder timeout(Duration timeout) {
    this.connectTimeoutMs = (int) timeout.toMillis();
    this.readTimeoutMs = (int) timeout.toMillis();
    return self();
  }

  @Override
  public LlmClientBuilder retry(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return self();
  }

  @Override
  public LlmClient build() {
    if (apiKey == null || apiKey.isEmpty()) {
      throw new IllegalStateException("API key is required");
    }
    return doBuild();
  }
}
