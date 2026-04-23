package io.llmkit.internal;

import io.llmkit.LlmClient;
import io.llmkit.RetryPolicy;
import io.llmkit.StreamListener;
import io.llmkit.internal.http.HttpClient;
import io.llmkit.internal.sse.SseParser;
import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Base class for LLM client implementations. Subclasses provide provider-specific encoding/decoding
 * via Codec.
 */
public abstract class AbstractLlmClient implements LlmClient {

  protected HttpClient httpClient;
  protected final String apiKey;
  protected final String model;
  protected final String baseUrl;
  protected final RetryPolicy retryPolicy;

  protected AbstractLlmClient(AbstractLlmClientBuilder<?> builder) {
    this.apiKey = builder.apiKey;
    this.model = builder.model;
    this.baseUrl = builder.baseUrl != null ? builder.baseUrl : defaultBaseUrl();
    this.retryPolicy = builder.retryPolicy != null ? builder.retryPolicy : RetryPolicy.defaults();
    this.httpClient =
        new io.llmkit.internal.http.JdkHttpClient(builder.connectTimeoutMs, builder.readTimeoutMs);
  }

  /** The provider-specific default base URL. */
  protected abstract String defaultBaseUrl();

  /** The provider-specific API endpoint path (e.g. "/v1/chat/completions"). */
  protected abstract String endpointPath();

  /** Build request headers for the provider. */
  protected abstract Map<String, String> buildHeaders();

  /** Encode a ChatRequest to a JSON request body. */
  protected abstract String encodeRequest(ChatRequest request);

  /** Decode a JSON response body to a ChatResponse. */
  protected abstract ChatResponse decodeResponse(String json);

  /** Decode a streaming chunk JSON to a ChatChunk. */
  protected abstract ChatChunk decodeChunk(String json);

  @Override
  public ChatResponse chat(ChatRequest request) {
    ChatRequest resolved = resolveModel(request);
    String body = encodeRequest(resolved);
    HttpClient.Request httpRequest =
        new HttpClient.Request(baseUrl + endpointPath(), buildHeaders(), body);

    Exception lastException = null;
    int attempts = retryPolicy.getMaxRetries() + 1;

    for (int i = 0; i < attempts; i++) {
      try {
        HttpClient.Response response = httpClient.post(httpRequest);
        if (response.getStatusCode() == 429 || response.getStatusCode() >= 500) {
          lastException =
              new IOException("HTTP " + response.getStatusCode() + ": " + response.getBody());
          if (i < attempts - 1) {
            sleep(calculateDelay(i));
            continue;
          }
        }
        if (!response.isSuccessful()) {
          throw new IOException("HTTP " + response.getStatusCode() + ": " + response.getBody());
        }
        return decodeResponse(response.getBody());
      } catch (Exception e) {
        lastException = e;
        if (i < attempts - 1) {
          sleep(calculateDelay(i));
        }
      }
    }
    throw new RuntimeException("Request failed after " + attempts + " attempts", lastException);
  }

  @Override
  public void chatStream(ChatRequest request, StreamListener listener) {
    ChatRequest resolved = resolveModel(request);
    String body = "{\"stream\":true," + encodeRequest(resolved).substring(1);
    HttpClient.Request httpRequest =
        new HttpClient.Request(baseUrl + endpointPath(), buildHeaders(), body);

    SseParser parser = null;
    try {
      HttpClient.ResponseStream stream = httpClient.postStream(httpRequest);
      parser = new SseParser(stream);

      String eventData;
      while ((eventData = parser.nextEvent()) != null) {
        if (eventData.trim().isEmpty()) continue;
        ChatChunk chunk = decodeChunk(eventData);
        if (chunk != null) {
          listener.onChunk(chunk);
        }
      }

      // Build a synthetic complete response
      ChatResponse completeResponse = decodeStreamComplete(eventData);
      listener.onComplete(completeResponse);
    } catch (Exception e) {
      listener.onError(e);
    } finally {
      if (parser != null) {
        try {
          parser.close();
        } catch (IOException ignored) {
        }
      }
    }
  }

  /** Override to provide custom stream completion behavior. */
  protected ChatResponse decodeStreamComplete(String lastData) {
    return new ChatResponse(
        null, java.util.Collections.<ChatResponse.Choice>emptyList(), null, model);
  }

  @Override
  public void close() {
    try {
      httpClient.close();
    } catch (IOException ignored) {
    }
  }

  private ChatRequest resolveModel(ChatRequest request) {
    if (request.getModel() != null) {
      return request;
    }
    return io.llmkit.model.ChatRequest.builder()
        .model(this.model)
        .messages(request.getMessages())
        .temperature(request.getTemperature())
        .maxTokens(request.getMaxTokens())
        .tools(request.getTools())
        .build();
  }

  private long calculateDelay(int attempt) {
    long delay =
        (long)
            (retryPolicy.getInitialDelayMs()
                * Math.pow(retryPolicy.getBackoffMultiplier(), attempt));
    return Math.min(delay, retryPolicy.getMaxDelayMs());
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
