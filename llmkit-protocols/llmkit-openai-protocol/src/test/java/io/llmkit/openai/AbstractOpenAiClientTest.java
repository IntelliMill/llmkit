package io.llmkit.openai;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AbstractOpenAiClientTest {

  /** Concrete subclass for testing the abstract class. */
  private static class TestClient extends AbstractOpenAiClient {

    TestClient(TestClientBuilder builder) {
      super(builder);
    }

    @Override
    protected String defaultBaseUrl() {
      return "https://test.example.com";
    }

    @Override
    protected String endpointPath() {
      return "/v1/chat/completions";
    }
  }

  private static class TestClientBuilder extends AbstractOpenAiClientBuilder<TestClientBuilder> {

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
  void buildHeadersContainsBearerAuth() {
    TestClient client = (TestClient) new TestClientBuilder().apiKey("sk-test123").build();
    Map<String, String> headers = client.buildHeaders();

    assertEquals("application/json", headers.get("Content-Type"));
    assertEquals("Bearer sk-test123", headers.get("Authorization"));
    assertEquals(2, headers.size());
  }

  @Test
  void encodeRequestDelegatesToCodec() {
    TestClient client =
        (TestClient) new TestClientBuilder().apiKey("sk-test").model("gpt-4o").build();
    String json = client.encodeRequest(ChatRequest.builder().model("gpt-4o").build());
    assertNotNull(json);
    assertTrue(json.contains("\"model\""));
    assertTrue(json.contains("gpt-4o"));
  }

  @Test
  void decodeResponseDelegatesToCodec() {
    TestClient client =
        (TestClient) new TestClientBuilder().apiKey("sk-test").model("gpt-4o").build();
    String json =
        "{\"id\":\"test\",\"model\":\"gpt-4o\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"hello\"},\"finish_reason\":\"stop\"}]}";
    ChatResponse response = client.decodeResponse(json);
    assertEquals("test", response.getId());
    assertEquals("hello", response.content());
  }

  @Test
  void decodeChunkDelegatesToCodec() {
    TestClient client =
        (TestClient) new TestClientBuilder().apiKey("sk-test").model("gpt-4o").build();
    String json =
        "{\"id\":\"test\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"hi\"},\"finish_reason\":null}]}";
    ChatChunk chunk = client.decodeChunk(json);
    assertEquals("test", chunk.getId());
    assertEquals("hi", chunk.getChoices().get(0).getDelta().getContent());
  }
}
