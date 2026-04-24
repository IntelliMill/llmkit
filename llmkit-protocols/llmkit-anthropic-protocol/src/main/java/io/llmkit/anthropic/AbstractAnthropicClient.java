package io.llmkit.anthropic;

import io.llmkit.internal.AbstractLlmClient;
import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base client for Anthropic-compatible API providers.
 *
 * <p>Provides the common Anthropic authentication headers and delegates encoding/decoding to {@link
 * AnthropicCodec}. Subclasses only need to provide {@code defaultBaseUrl()}, {@code
 * endpointPath()}, and a constructor.
 */
public abstract class AbstractAnthropicClient extends AbstractLlmClient {

  protected AbstractAnthropicClient(AbstractAnthropicClientBuilder<?> builder) {
    super(builder);
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
