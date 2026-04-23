package io.llmkit.internal.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JsonReaderTest {

  @Test
  void readStringField() {
    JsonReader r = new JsonReader("{\"name\":\"hello\"}");
    assertEquals("hello", r.readString("name"));
  }

  @Test
  void readStringWithSpaces() {
    JsonReader r = new JsonReader("{\"name\": \"hello\"}");
    assertEquals("hello", r.readString("name"));
  }

  @Test
  void readMissingKeyReturnsNull() {
    JsonReader r = new JsonReader("{\"name\":\"hello\"}");
    assertNull(r.readString("missing"));
  }

  @Test
  void readIntField() {
    JsonReader r = new JsonReader("{\"count\":42}");
    assertEquals(42, r.readInt("count"));
  }

  @Test
  void readNegativeInt() {
    JsonReader r = new JsonReader("{\"val\":-5}");
    assertEquals(-5, r.readInt("val"));
  }

  @Test
  void readMissingIntReturnsNull() {
    JsonReader r = new JsonReader("{\"a\":1}");
    assertNull(r.readInt("b"));
  }

  @Test
  void findArray() {
    JsonReader r = new JsonReader("{\"items\":[1,2,3]}");
    int pos = r.findArray("items");
    assertTrue(pos > 0);
  }

  @Test
  void findArrayMissingReturnsNeg1() {
    JsonReader r = new JsonReader("{\"a\":1}");
    assertEquals(-1, r.findArray("items"));
  }

  @Test
  void findObjectBounds() {
    JsonReader r = new JsonReader("[{\"a\":1},{\"b\":2}]");
    int[] bounds = r.findObjectBounds(1);
    assertNotNull(bounds);
    assertEquals(1, bounds[0]);
    String obj = r.getJson().substring(bounds[0], bounds[1]);
    assertEquals("{\"a\":1}", obj);
  }

  @Test
  void findObjectBoundsNested() {
    JsonReader r = new JsonReader("{\"outer\":{\"inner\":\"val\"}}");
    int[] bounds = r.findObjectBounds(0);
    assertNotNull(bounds);
    String obj = r.getJson().substring(bounds[0], bounds[1]);
    assertEquals("{\"outer\":{\"inner\":\"val\"}}", obj);
  }

  @Test
  void subReader() {
    JsonReader r = new JsonReader("[{\"a\":1},{\"b\":2}]");
    int[] bounds = r.findObjectBounds(1);
    JsonReader sub = r.subReader(bounds[0], bounds[1]);
    assertEquals(1, sub.readInt("a"));
  }

  @Test
  void escapedCharacters() {
    JsonReader r = new JsonReader("{\"text\":\"hello\\nworld\\t!\"}");
    String val = r.readString("text");
    assertEquals("hello\nworld\t!", val);
  }

  @Test
  void escapedQuote() {
    JsonReader r = new JsonReader("{\"text\":\"say \\\"hi\\\"\"}");
    assertEquals("say \"hi\"", r.readString("text"));
  }

  @Test
  void escapedUnicode() {
    JsonReader r = new JsonReader("{\"text\":\"\\u0041\"}");
    assertEquals("A", r.readString("text"));
  }

  @Test
  void escapedBackslash() {
    JsonReader r = new JsonReader("{\"text\":\"path\\\\file\"}");
    assertEquals("path\\file", r.readString("text"));
  }

  @Test
  void setPositionAndGetPosition() {
    JsonReader r = new JsonReader("{\"a\":1,\"b\":2}");
    r.setPosition(5);
    assertEquals(5, r.getPosition());
    assertEquals(2, r.readInt("b"));
  }
}
