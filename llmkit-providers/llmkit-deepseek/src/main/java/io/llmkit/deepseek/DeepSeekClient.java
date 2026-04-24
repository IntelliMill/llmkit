package io.llmkit.deepseek;

import io.llmkit.openai.AbstractOpenAiClient;

/** DeepSeek Chat Completions API client. */
final class DeepSeekClient extends AbstractOpenAiClient {

  private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
  private static final String ENDPOINT = "/v1/chat/completions";

  DeepSeekClient(DeepSeekClientBuilder builder) {
    super(builder);
  }

  @Override
  protected String defaultBaseUrl() {
    return DEFAULT_BASE_URL;
  }

  @Override
  protected String endpointPath() {
    return ENDPOINT;
  }
}
