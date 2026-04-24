package io.llmkit.qwen;

import io.llmkit.openai.AbstractOpenAiClient;

/** Qwen (DashScope) Chat Completions API client. */
final class QwenClient extends AbstractOpenAiClient {

  private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode";
  private static final String ENDPOINT = "/v1/chat/completions";

  QwenClient(QwenClientBuilder builder) {
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
