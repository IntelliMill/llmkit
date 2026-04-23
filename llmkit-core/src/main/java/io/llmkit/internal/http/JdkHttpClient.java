package io.llmkit.internal.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** JDK HttpURLConnection-based HTTP client implementation. Zero external dependencies. */
public final class JdkHttpClient implements HttpClient {

  private final int connectTimeoutMs;
  private final int readTimeoutMs;

  public JdkHttpClient(int connectTimeoutMs, int readTimeoutMs) {
    this.connectTimeoutMs = connectTimeoutMs;
    this.readTimeoutMs = readTimeoutMs;
  }

  @Override
  public Response post(Request request) throws Exception {
    HttpURLConnection conn = openConnection(request.getUrl());
    applyTimeouts(conn);
    applyHeaders(conn, request.getHeaders());
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);

    writeBody(conn, request.getBody());

    int statusCode = conn.getResponseCode();
    String body = readResponseBody(conn);
    conn.disconnect();
    return new Response(statusCode, body);
  }

  @Override
  public ResponseStream postStream(Request request) throws Exception {
    HttpURLConnection conn = openConnection(request.getUrl());
    applyTimeouts(conn);
    applyHeaders(conn, request.getHeaders());
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);

    writeBody(conn, request.getBody());

    int statusCode = conn.getResponseCode();
    if (statusCode >= 200 && statusCode < 300) {
      InputStream stream = conn.getInputStream();
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
      return new JdkResponseStream(conn, reader);
    } else {
      String errorBody = readResponseBody(conn);
      conn.disconnect();
      throw new IOException("HTTP " + statusCode + ": " + errorBody);
    }
  }

  @Override
  public void close() {}

  private HttpURLConnection openConnection(String url) throws Exception {
    return (HttpURLConnection) new URL(url).openConnection();
  }

  private void applyTimeouts(HttpURLConnection conn) {
    conn.setConnectTimeout(connectTimeoutMs);
    conn.setReadTimeout(readTimeoutMs);
  }

  private void applyHeaders(HttpURLConnection conn, Map<String, String> headers) {
    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        conn.setRequestProperty(entry.getKey(), entry.getValue());
      }
    }
  }

  private void writeBody(HttpURLConnection conn, String body) throws IOException {
    if (body != null) {
      byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
      conn.setFixedLengthStreamingMode(bytes.length);
      OutputStream out = conn.getOutputStream();
      try {
        out.write(bytes);
        out.flush();
      } finally {
        out.close();
      }
    }
  }

  private String readResponseBody(HttpURLConnection conn) throws IOException {
    InputStream stream = conn.getErrorStream();
    if (stream == null) {
      stream = conn.getInputStream();
    }
    if (stream == null) {
      return "";
    }
    try {
      StringBuilder sb = new StringBuilder();
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
      String line;
      while ((line = reader.readLine()) != null) {
        if (sb.length() > 0) {
          sb.append('\n');
        }
        sb.append(line);
      }
      return sb.toString();
    } finally {
      stream.close();
    }
  }

  private static final class JdkResponseStream implements ResponseStream {
    private final HttpURLConnection conn;
    private final BufferedReader reader;

    JdkResponseStream(HttpURLConnection conn, BufferedReader reader) {
      this.conn = conn;
      this.reader = reader;
    }

    @Override
    public String readLine() throws Exception {
      return reader.readLine();
    }

    @Override
    public void close() throws IOException {
      reader.close();
      conn.disconnect();
    }
  }
}
