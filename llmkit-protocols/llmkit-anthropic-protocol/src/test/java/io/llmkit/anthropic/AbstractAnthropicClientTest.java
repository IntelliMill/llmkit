package io.llmkit.anthropic;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AbstractAnthropicClientTest {

  /** Concrete subclass for testing the abstract class. */
  private static class TestClient extends AbstractAnthropicClient {

    TestClient(TestClientBuilder builder) {
      super(builder);
    }

    @Override
    protected String defaultBaseUrl() {
      return "https://test.example.com";
    }

    @Override
    protected String endpointPath() {
      return "/v1/messages";
    }
  }

  private static class TestClientBuilder extends AbstractAnthropicClientBuilder<TestClientBuilder> {

    @Override
    protected TestClientBuilder self() {
      return this;
    }

    @Override
    protected io.llmkit.LlmClient doBuild() {
      return new TestClient(this);
    }
  }

  @Test
  void buildHeadersContainsApiKeyAndVersion() {
    TestClient client = (TestClient) new TestClientBuilder().apiKey("sk-ant-test123").build();
    Map<String, String> headers = client.buildHeaders();

    assertEquals("application/json", headers.get("Content-Type"));
    assertEquals("sk-ant-test123", headers.get("x-api-key"));
    assertEquals("2023-06-01", headers.get("anthropic-version"));
    assertEquals(3, headers.size());
  }

  @Test
  void encodeRequestDelegatesToCodec() {
    TestClient client =
        (TestClient)
            new TestClientBuilder()
                .apiKey("sk-ant-test")
                .model("claude-3-5-sonnet-20241022")
                .build();
    String json =
        client.encodeRequest(
            ChatRequest.builder().model("claude-3-5-sonnet-20241022").maxTokens(100).build());
    assertNotNull(json);
    assertTrue(json.contains("\"model\""));
    assertTrue(json.contains("claude-3-5-sonnet-20241022"));
    assertTrue(json.contains("\"max_tokens\""));
  }

  @Test
  void decodeResponseDelegatesToCodec() {
    TestClient client =
        (TestClient)
            new TestClientBuilder()
                .apiKey("sk-ant-test")
                .model("claude-3-5-sonnet-20241022")
                .build();
    String json =
        "{\"id\":\"msg_test\",\"type\":\"message\",\"role\":\"assistant\",\"model\":\"claude-3-5-sonnet-20241022\",\"content\":[{\"type\":\"text\",\"text\":\"hello\"}],\"stop_reason\":\"end_turn\",\"usage\":{\"input_tokens\":10,\"output_tokens\":5}}";
    ChatResponse response = client.decodeResponse(json);
    assertEquals("msg_test", response.getId());
    assertEquals("hello", response.content());
  }

  @Test
  void decodeChunkContentBlockDelta() {
    TestClient client =
        (TestClient)
            new TestClientBuilder()
                .apiKey("sk-ant-test")
                .model("claude-3-5-sonnet-20241022")
                .build();
    String json =
        "{\"type\":\"content_block_delta\",\"index\":0,\"delta\":{\"type\":\"text_delta\",\"text\":\"hi\"}}";
    ChatChunk chunk = client.decodeChunk(json);
    assertNotNull(chunk);
    assertEquals("hi", chunk.getChoices().get(0).getDelta().getContent());
  }

  @Test
  void decodeChunkReturnsNullForNonContentEvents() {
    TestClient client =
        (TestClient)
            new TestClientBuilder()
                .apiKey("sk-ant-test")
                .model("claude-3-5-sonnet-20241022")
                .build();
    assertNull(client.decodeChunk("{\"type\":\"ping\"}"));
    assertNull(client.decodeChunk("{\"type\":\"message_start\"}"));
    assertNull(client.decodeChunk("{\"type\":\"message_stop\"}"));
  }
}
