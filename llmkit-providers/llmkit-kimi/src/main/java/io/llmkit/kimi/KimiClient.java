package io.llmkit.kimi;

import io.llmkit.openai.AbstractOpenAiClient;

/** Kimi (Moonshot AI) Chat Completions API client. */
final class KimiClient extends AbstractOpenAiClient {

  private static final String DEFAULT_BASE_URL = "https://api.moonshot.cn";
  private static final String ENDPOINT = "/v1/chat/completions";

  KimiClient(KimiClientBuilder builder) {
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
