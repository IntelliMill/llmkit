package io.llmkit.internal.json;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class JsonWriterTest {

  @Test
  void emptyObject() {
    String json = new JsonWriter().build();
    assertEquals("{}", json);
  }

  @Test
  void stringField() {
    String json = new JsonWriter().field("name", "hello").build();
    assertEquals("{\"name\":\"hello\"}", json);
  }

  @Test
  void nullStringFieldSkipped() {
    String json = new JsonWriter().field("name", (String) null).field("age", "10").build();
    assertEquals("{\"age\":\"10\"}", json);
  }

  @Test
  void intField() {
    String json = new JsonWriter().field("count", 42).build();
    assertEquals("{\"count\":42}", json);
  }

  @Test
  void doubleField() {
    String json = new JsonWriter().field("temp", 0.7).build();
    assertEquals("{\"temp\":0.7}", json);
  }

  @Test
  void booleanField() {
    String json = new JsonWriter().field("stream", true).build();
    assertEquals("{\"stream\":true}", json);
  }

  @Test
  void multipleFields() {
    String json = new JsonWriter().field("a", "1").field("b", 2).field("c", 3.0).build();
    assertEquals("{\"a\":\"1\",\"b\":2,\"c\":3.0}", json);
  }

  @Test
  void arrayField() {
    String json =
        new JsonWriter()
            .field("model", "gpt")
            .array("items")
            .raw("\"a\"")
            .raw("\"b\"")
            .end()
            .build();
    assertEquals("{\"model\":\"gpt\",\"items\":[\"a\",\"b\"]}", json);
  }

  @Test
  void arrayWithString() {
    String json = new JsonWriter().array("list").string("hello").end().build();
    assertEquals("{\"list\":[\"hello\"]}", json);
  }

  @Test
  void rawField() {
    String json = new JsonWriter().rawField("data", "{\"nested\":true}").build();
    assertEquals("{\"data\":{\"nested\":true}}", json);
  }

  @Test
  void escaping() {
    String json = new JsonWriter().field("text", "line1\nline2\ttab\"quote\\backslash").build();
    assertEquals("{\"text\":\"line1\\nline2\\ttab\\\"quote\\\\backslash\"}", json);
  }

  @Test
  void arrayWithRawNestedObjects() {
    String json =
        new JsonWriter()
            .array("messages")
            .raw(new JsonWriter().field("role", "user").field("content", "hi").build())
            .end()
            .build();
    assertEquals("{\"messages\":[{\"role\":\"user\",\"content\":\"hi\"}]}", json);
  }

  @Test
  void nullIntFieldSkipped() {
    String json = new JsonWriter().field("x", (Integer) null).field("y", 1).build();
    assertEquals("{\"y\":1}", json);
  }

  @Test
  void nullDoubleFieldSkipped() {
    String json = new JsonWriter().field("x", (Double) null).field("y", 1.0).build();
    assertEquals("{\"y\":1.0}", json);
  }

  @Test
  void nullBooleanFieldSkipped() {
    String json = new JsonWriter().field("x", (Boolean) null).field("y", true).build();
    assertEquals("{\"y\":true}", json);
  }
}
