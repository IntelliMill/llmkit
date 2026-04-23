package io.llmkit;

import java.time.Duration;

/** Builder interface for constructing LlmClient instances. */
public interface LlmClientBuilder {

  LlmClientBuilder apiKey(String apiKey);

  LlmClientBuilder model(String model);

  LlmClientBuilder baseUrl(String baseUrl);

  LlmClientBuilder timeout(Duration timeout);

  LlmClientBuilder retry(RetryPolicy retryPolicy);

  LlmClient build();
}
