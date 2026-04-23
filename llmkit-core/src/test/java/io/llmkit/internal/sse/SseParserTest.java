package io.llmkit.internal.sse;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.internal.http.HttpClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SseParserTest {

  private static class MockResponseStream implements HttpClient.ResponseStream {
    private final List<String> lines;
    private int index = 0;

    MockResponseStream(List<String> lines) {
      this.lines = lines;
    }

    @Override
    public String readLine() {
      if (index >= lines.size()) return null;
      return lines.get(index++);
    }

    @Override
    public void close() {}
  }

  @Test
  void singleEvent() throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("data: {\"content\":\"hello\"}");
    lines.add("");

    SseParser parser = new SseParser(new MockResponseStream(lines));
    assertEquals("{\"content\":\"hello\"}", parser.nextEvent());
    assertNull(parser.nextEvent());
    parser.close();
  }

  @Test
  void multipleEvents() throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("data: event1");
    lines.add("");
    lines.add("data: event2");
    lines.add("");

    SseParser parser = new SseParser(new MockResponseStream(lines));
    assertEquals("event1", parser.nextEvent());
    assertEquals("event2", parser.nextEvent());
    assertNull(parser.nextEvent());
    parser.close();
  }

  @Test
  void doneSignalStops() throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("data: {\"text\":\"hi\"}");
    lines.add("");
    lines.add("data: [DONE]");
    lines.add("");

    SseParser parser = new SseParser(new MockResponseStream(lines));
    assertEquals("{\"text\":\"hi\"}", parser.nextEvent());
    assertNull(parser.nextEvent());
    parser.close();
  }

  @Test
  void ignoresNonDataLines() throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("event: message");
    lines.add("id: 123");
    lines.add("data: payload");
    lines.add("");

    SseParser parser = new SseParser(new MockResponseStream(lines));
    assertEquals("payload", parser.nextEvent());
    parser.close();
  }

  @Test
  void multilineData() throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("data: line1");
    lines.add("data: line2");
    lines.add("");

    SseParser parser = new SseParser(new MockResponseStream(lines));
    assertEquals("line1\nline2", parser.nextEvent());
    parser.close();
  }

  @Test
  void emptyStream() throws IOException {
    SseParser parser = new SseParser(new MockResponseStream(new ArrayList<>()));
    assertNull(parser.nextEvent());
    parser.close();
  }

  @Test
  void noColonVariant() throws IOException {
    List<String> lines = new ArrayList<>();
    lines.add("data:hello");
    lines.add("");

    SseParser parser = new SseParser(new MockResponseStream(lines));
    assertEquals("hello", parser.nextEvent());
    parser.close();
  }
}
