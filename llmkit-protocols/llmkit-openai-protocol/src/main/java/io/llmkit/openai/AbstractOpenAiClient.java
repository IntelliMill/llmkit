package io.llmkit.openai;

import io.llmkit.internal.AbstractLlmClient;
import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base client for OpenAI-compatible API providers.
 *
 * <p>Provides the common Bearer token authentication header and delegates encoding/decoding to
 * {@link OpenAiCodec}. Subclasses only need to provide {@code defaultBaseUrl()}, {@code
 * endpointPath()}, and a constructor.
 */
public abstract class AbstractOpenAiClient extends AbstractLlmClient {

  protected AbstractOpenAiClient(AbstractOpenAiClientBuilder<?> builder) {
    super(builder);
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
