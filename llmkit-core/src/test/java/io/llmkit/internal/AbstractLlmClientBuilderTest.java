package io.llmkit.internal;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.LlmClient;
import io.llmkit.RetryPolicy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AbstractLlmClientBuilderTest {

  private static class TestClient extends AbstractLlmClient {
    TestClient(TestBuilder builder) {
      super(builder);
    }

    @Override
    protected String defaultBaseUrl() {
      return "https://default.example.com";
    }

    @Override
    protected String endpointPath() {
      return "/v1/test";
    }

    @Override
    protected Map<String, String> buildHeaders() {
      return new HashMap<>();
    }

    @Override
    protected String encodeRequest(io.llmkit.model.ChatRequest request) {
      return "{}";
    }

    @Override
    protected io.llmkit.model.ChatResponse decodeResponse(String json) {
      return null;
    }

    @Override
    protected io.llmkit.model.ChatChunk decodeChunk(String json) {
      return null;
    }
  }

  private static class TestBuilder extends AbstractLlmClientBuilder<TestBuilder> {
    @Override
    protected TestBuilder self() {
      return this;
    }

    @Override
    protected LlmClient doBuild() {
      return new TestClient(this);
    }
  }

  @Test
  void buildRequiresApiKey() {
    assertThrows(IllegalStateException.class, () -> new TestBuilder().build());
  }

  @Test
  void buildWithEmptyApiKeyFails() {
    assertThrows(IllegalStateException.class, () -> new TestBuilder().apiKey("").build());
  }

  @Test
  void defaultTimeouts() {
    TestBuilder b = new TestBuilder();
    assertEquals(10000, b.connectTimeoutMs);
    assertEquals(60000, b.readTimeoutMs);
  }

  @Test
  void customTimeoutApplied() {
    TestClient client =
        (TestClient) new TestBuilder().apiKey("key").timeout(Duration.ofSeconds(30)).build();
    assertNotNull(client);
  }

  @Test
  void customRetryPolicyApplied() {
    RetryPolicy policy = RetryPolicy.builder().maxRetries(5).build();
    TestClient client = (TestClient) new TestBuilder().apiKey("key").retry(policy).build();
    assertNotNull(client);
  }

  @Test
  void customBaseUrlApplied() {
    TestClient client =
        (TestClient) new TestBuilder().apiKey("key").baseUrl("https://custom.api.com").build();
    assertNotNull(client);
  }

  @Test
  void modelFieldSettable() {
    TestBuilder b = new TestBuilder();
    b.model("gpt-4o");
    assertEquals("gpt-4o", b.model);
  }

  @Test
  void baseUrlFieldSettable() {
    TestBuilder b = new TestBuilder();
    b.baseUrl("https://example.com");
    assertEquals("https://example.com", b.baseUrl);
  }

  @Test
  void apiKeyFieldSettable() {
    TestBuilder b = new TestBuilder();
    b.apiKey("sk-test");
    assertEquals("sk-test", b.apiKey);
  }
}
