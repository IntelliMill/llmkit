package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ToolCallTest {

  @Test
  void allFields() {
    ToolCall tc = new ToolCall("call-1", "get_weather", "{\"city\":\"Tokyo\"}");
    assertEquals("call-1", tc.getId());
    assertEquals("get_weather", tc.getName());
    assertEquals("{\"city\":\"Tokyo\"}", tc.getArguments());
  }

  @Test
  void nullArguments() {
    ToolCall tc = new ToolCall("id", "func", null);
    assertNull(tc.getArguments());
  }
}
