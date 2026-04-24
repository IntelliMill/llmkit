package io.llmkit.anthropic;

/** Anthropic Messages API client. */
final class AnthropicClient extends AbstractAnthropicClient {

  private static final String DEFAULT_BASE_URL = "https://api.anthropic.com";
  private static final String ENDPOINT = "/v1/messages";

  AnthropicClient(AnthropicClientBuilder builder) {
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
