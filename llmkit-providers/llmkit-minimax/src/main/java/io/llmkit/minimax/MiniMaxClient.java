package io.llmkit.minimax;

import io.llmkit.openai.AbstractOpenAiClient;

/** MiniMax Chat Completions API client. */
final class MiniMaxClient extends AbstractOpenAiClient {

  private static final String DEFAULT_BASE_URL = "https://api.minimax.chat";
  private static final String ENDPOINT = "/v1/text/chatcompletion_v2";

  MiniMaxClient(MiniMaxClientBuilder builder) {
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
