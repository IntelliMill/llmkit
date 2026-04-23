package io.llmkit.anthropic;

import io.llmkit.internal.AbstractLlmClient;
import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import java.util.HashMap;
import java.util.Map;

/** Anthropic Messages API client. */
final class AnthropicClient extends AbstractLlmClient {

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

  @Override
  protected Map<String, String> buildHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    headers.put("x-api-key", apiKey);
    headers.put("anthropic-version", "2023-06-01");
    return headers;
  }

  @Override
  protected String encodeRequest(ChatRequest request) {
    return AnthropicCodec.encodeRequest(request, apiKey);
  }

  @Override
  protected ChatResponse decodeResponse(String json) {
    return AnthropicCodec.decodeResponse(json);
  }

  @Override
  protected ChatChunk decodeChunk(String json) {
    return AnthropicCodec.decodeChunk(json);
  }
}
