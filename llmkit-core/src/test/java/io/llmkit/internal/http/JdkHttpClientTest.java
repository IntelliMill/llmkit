package io.llmkit.internal.http;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JdkHttpClientTest {

  @Test
  void constructorSetsTimeouts() {
    JdkHttpClient client = new JdkHttpClient(5000, 10000);
    assertNotNull(client);
  }

  @Test
  void closeDoesNotThrow() {
    JdkHttpClient client = new JdkHttpClient(5000, 10000);
    assertDoesNotThrow(() -> client.close());
  }

  @Test
  void requestHoldsValues() {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    HttpClient.Request req = new HttpClient.Request("https://example.com", headers, "{\"a\":1}");
    assertEquals("https://example.com", req.getUrl());
    assertEquals("application/json", req.getHeaders().get("Content-Type"));
    assertEquals("{\"a\":1}", req.getBody());
  }

  @Test
  void responseStatusCodeAndBody() {
    HttpClient.Response resp = new HttpClient.Response(200, "ok");
    assertEquals(200, resp.getStatusCode());
    assertEquals("ok", resp.getBody());
    assertTrue(resp.isSuccessful());
  }

  @Test
  void responseNonSuccessful() {
    HttpClient.Response resp404 = new HttpClient.Response(404, "not found");
    assertFalse(resp404.isSuccessful());
    HttpClient.Response resp500 = new HttpClient.Response(500, "error");
    assertFalse(resp500.isSuccessful());
    HttpClient.Response resp429 = new HttpClient.Response(429, "rate limited");
    assertFalse(resp429.isSuccessful());
  }

  @Test
  void responseSuccessRange() {
    assertTrue(new HttpClient.Response(200, "").isSuccessful());
    assertTrue(new HttpClient.Response(201, "").isSuccessful());
    assertTrue(new HttpClient.Response(299, "").isSuccessful());
    assertFalse(new HttpClient.Response(300, "").isSuccessful());
  }
}
