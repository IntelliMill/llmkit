package io.llmkit.internal.http;

import java.io.Closeable;
import java.util.Map;

/** Minimal HTTP client abstraction. */
public interface HttpClient extends Closeable {

  Response post(Request request) throws Exception;

  ResponseStream postStream(Request request) throws Exception;

  final class Request {
    private final String url;
    private final Map<String, String> headers;
    private final String body;

    public Request(String url, Map<String, String> headers, String body) {
      this.url = url;
      this.headers = headers;
      this.body = body;
    }

    public String getUrl() {
      return url;
    }

    public Map<String, String> getHeaders() {
      return headers;
    }

    public String getBody() {
      return body;
    }
  }

  final class Response {
    private final int statusCode;
    private final String body;

    public Response(int statusCode, String body) {
      this.statusCode = statusCode;
      this.body = body;
    }

    public int getStatusCode() {
      return statusCode;
    }

    public String getBody() {
      return body;
    }

    public boolean isSuccessful() {
      return statusCode >= 200 && statusCode < 300;
    }
  }

  interface ResponseStream extends Closeable {
    /** Read the next line from the stream, or null if the stream is done. */
    String readLine() throws Exception;
  }
}
