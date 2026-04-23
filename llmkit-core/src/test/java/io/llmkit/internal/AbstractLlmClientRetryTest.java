package io.llmkit.internal;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmClient;
import io.llmkit.RetryPolicy;
import io.llmkit.internal.http.HttpClient;
import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AbstractLlmClientRetryTest {

  /** A mock HttpClient that records calls and returns configurable responses. */
  private static class MockHttpClient implements HttpClient {
    private final List<Request> recordedRequests = new ArrayList<>();
    private final List<Response> responses;
    private int callIndex = 0;

    MockHttpClient(List<Response> responses) {
      this.responses = responses;
    }

    @Override
    public Response post(Request request) {
      recordedRequests.add(request);
      if (callIndex < responses.size()) {
        return responses.get(callIndex++);
      }
      return new Response(500, "no more mock responses");
    }

    @Override
    public ResponseStream postStream(Request request) throws Exception {
      throw new UnsupportedOperationException("not used in retry tests");
    }

    @Override
    public void close() {}

    List<Request> getRecordedRequests() {
      return recordedRequests;
    }
  }

  private static class RetryTestClient extends AbstractLlmClient {
    private final MockHttpClient mockHttp;

    RetryTestClient(MockHttpClient mockHttp, RetryPolicy retryPolicy) {
      super(new RetryTestBuilder(retryPolicy));
      this.mockHttp = mockHttp;
      this.httpClient = mockHttp; // replace with mock
    }

    @Override
    protected String defaultBaseUrl() {
      return "https://api.test.com";
    }

    @Override
    protected String endpointPath() {
      return "/v1/chat";
    }

    @Override
    protected Map<String, String> buildHeaders() {
      return new HashMap<>();
    }

    @Override
    protected String encodeRequest(ChatRequest request) {
      return "{}";
    }

    @Override
    protected ChatResponse decodeResponse(String json) {
      return new ChatResponse("id", new ArrayList<>(), null, "test");
    }

    @Override
    protected ChatChunk decodeChunk(String json) {
      return null;
    }
  }

  private static class RetryTestBuilder extends AbstractLlmClientBuilder<RetryTestBuilder> {
    RetryTestBuilder(RetryPolicy retryPolicy) {
      this.apiKey = "test-key";
      this.model = "test-model";
      this.retryPolicy = retryPolicy;
    }

    @Override
    protected RetryTestBuilder self() {
      return this;
    }

    @Override
    protected LlmClient doBuild() {
      throw new UnsupportedOperationException("use RetryTestClient constructor directly");
    }
  }

  @Test
  void retryOn500ThenSucceed() {
    // First call returns 500, second returns 200
    MockHttpClient mock =
        new MockHttpClient(
            java.util.Arrays.asList(
                new HttpClient.Response(500, "internal error"),
                new HttpClient.Response(200, "{\"id\":\"ok\"}")));

    RetryPolicy policy = RetryPolicy.builder().maxRetries(2).initialDelayMs(1).build();
    RetryTestClient client = new RetryTestClient(mock, policy);

    ChatResponse resp =
        client.chat(
            ChatRequest.builder().addMessage(io.llmkit.model.ChatMessage.user("hi")).build());
    assertNotNull(resp);
    assertEquals(2, mock.getRecordedRequests().size());
  }

  @Test
  void retryOn429ThenSucceed() {
    MockHttpClient mock =
        new MockHttpClient(
            java.util.Arrays.asList(
                new HttpClient.Response(429, "rate limited"),
                new HttpClient.Response(200, "{\"id\":\"ok\"}")));

    RetryPolicy policy = RetryPolicy.builder().maxRetries(2).initialDelayMs(1).build();
    RetryTestClient client = new RetryTestClient(mock, policy);

    ChatResponse resp =
        client.chat(
            ChatRequest.builder().addMessage(io.llmkit.model.ChatMessage.user("hi")).build());
    assertNotNull(resp);
    assertEquals(2, mock.getRecordedRequests().size());
  }

  @Test
  void allRetriesExhausted() {
    MockHttpClient mock =
        new MockHttpClient(
            java.util.Arrays.asList(
                new HttpClient.Response(500, "error1"),
                new HttpClient.Response(500, "error2"),
                new HttpClient.Response(500, "error3")));

    RetryPolicy policy = RetryPolicy.builder().maxRetries(2).initialDelayMs(1).build();
    RetryTestClient client = new RetryTestClient(mock, policy);

    RuntimeException ex =
        assertThrows(
            RuntimeException.class,
            () ->
                client.chat(
                    ChatRequest.builder()
                        .addMessage(io.llmkit.model.ChatMessage.user("hi"))
                        .build()));
    assertTrue(ex.getMessage().contains("3 attempts"));
    assertEquals(3, mock.getRecordedRequests().size());
  }

  @Test
  void retriesOn400ErrorUntilExhausted() {
    MockHttpClient mock =
        new MockHttpClient(java.util.Arrays.asList(new HttpClient.Response(400, "bad request")));

    RetryPolicy policy = RetryPolicy.builder().maxRetries(3).initialDelayMs(1).build();
    RetryTestClient client = new RetryTestClient(mock, policy);

    assertThrows(
        RuntimeException.class,
        () ->
            client.chat(
                ChatRequest.builder().addMessage(io.llmkit.model.ChatMessage.user("hi")).build()));
    // All attempts exhausted: 1 + 3 retries = 4
    assertEquals(4, mock.getRecordedRequests().size());
  }

  @Test
  void successOnFirstTry() {
    MockHttpClient mock =
        new MockHttpClient(
            java.util.Arrays.asList(new HttpClient.Response(200, "{\"id\":\"ok\"}")));

    RetryPolicy policy = RetryPolicy.builder().maxRetries(3).initialDelayMs(1).build();
    RetryTestClient client = new RetryTestClient(mock, policy);

    ChatResponse resp =
        client.chat(
            ChatRequest.builder().addMessage(io.llmkit.model.ChatMessage.user("hi")).build());
    assertNotNull(resp);
    assertEquals(1, mock.getRecordedRequests().size());
  }
}
