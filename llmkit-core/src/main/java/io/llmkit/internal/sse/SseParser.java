package io.llmkit.internal.sse;

import io.llmkit.internal.http.HttpClient;
import java.io.IOException;

/**
 * Parser for Server-Sent Events (SSE) streams. Follows the SSE specification: each event is
 * separated by a blank line, and data lines are prefixed with "data: ".
 */
public final class SseParser {

  private final HttpClient.ResponseStream stream;

  public SseParser(HttpClient.ResponseStream stream) {
    this.stream = stream;
  }

  /** Read the next SSE event data. Returns null when the stream is done. */
  public String nextEvent() throws IOException {
    StringBuilder data = new StringBuilder();
    boolean hasData = false;

    String line;
    while ((line = readLineSilently()) != null) {
      if (line.isEmpty()) {
        if (hasData) {
          return data.toString();
        }
        continue;
      }
      if (line.startsWith("data: ")) {
        String eventData = line.substring(6);
        if (eventData.equals("[DONE]")) {
          return null;
        }
        if (hasData) {
          data.append('\n');
        }
        data.append(eventData);
        hasData = true;
      } else if (line.startsWith("data:")) {
        // space-less variant
        String eventData = line.substring(5);
        if (eventData.equals("[DONE]")) {
          return null;
        }
        if (hasData) {
          data.append('\n');
        }
        data.append(eventData);
        hasData = true;
      }
      // Ignore other SSE fields (event:, id:, retry:, comments)
    }

    return hasData ? data.toString() : null;
  }

  private String readLineSilently() {
    try {
      return stream.readLine();
    } catch (Exception e) {
      return null;
    }
  }

  public void close() throws IOException {
    stream.close();
  }
}
