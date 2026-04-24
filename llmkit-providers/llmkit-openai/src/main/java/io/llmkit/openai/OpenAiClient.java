package io.llmkit.openai;

/** OpenAI Chat Completions API client. */
final class OpenAiClient extends AbstractOpenAiClient {

  private static final String DEFAULT_BASE_URL = "https://api.openai.com";
  private static final String ENDPOINT = "/v1/chat/completions";

  OpenAiClient(OpenAiClientBuilder builder) {
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
