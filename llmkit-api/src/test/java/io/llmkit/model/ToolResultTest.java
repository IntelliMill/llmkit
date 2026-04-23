package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ToolResultTest {

  @Test
  void successResult() {
    ToolResult tr = new ToolResult("call-1", "sunny");
    assertEquals("call-1", tr.getToolCallId());
    assertEquals("sunny", tr.getContent());
    assertFalse(tr.isError());
  }

  @Test
  void errorResult() {
    ToolResult tr = new ToolResult("call-2", "timeout", true);
    assertTrue(tr.isError());
  }
}
