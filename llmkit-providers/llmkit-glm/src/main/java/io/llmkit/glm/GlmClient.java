package io.llmkit.glm;

import io.llmkit.openai.AbstractOpenAiClient;

/** GLM (Zhipu AI) Chat Completions API client. */
final class GlmClient extends AbstractOpenAiClient {

  private static final String DEFAULT_BASE_URL = "https://open.bigmodel.cn/api/paas";
  private static final String ENDPOINT = "/v4/chat/completions";

  GlmClient(GlmClientBuilder builder) {
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
