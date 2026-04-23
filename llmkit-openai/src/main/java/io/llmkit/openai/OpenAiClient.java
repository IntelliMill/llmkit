package io.llmkit.openai;

import io.llmkit.internal.AbstractLlmClient;
import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import java.util.HashMap;
import java.util.Map;

/** OpenAI Chat Completions API client. */
final class OpenAiClient extends AbstractLlmClient {

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

  @Override
  protected Map<String, String> buildHeaders() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    headers.put("Authorization", "Bearer " + apiKey);
    return headers;
  }

  @Override
  protected String encodeRequest(ChatRequest request) {
    return OpenAiCodec.encodeRequest(request);
  }

  @Override
  protected ChatResponse decodeResponse(String json) {
    return OpenAiCodec.decodeResponse(json);
  }

  @Override
  protected ChatChunk decodeChunk(String json) {
    return OpenAiCodec.decodeChunk(json);
  }
}
