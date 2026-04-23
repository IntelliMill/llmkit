package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ToolDefinitionTest {

  @Test
  void allFields() {
    ToolDefinition td =
        ToolDefinition.builder()
            .name("get_weather")
            .description("Get weather")
            .parameters("{\"type\":\"object\"}")
            .build();
    assertEquals("get_weather", td.getName());
    assertEquals("Get weather", td.getDescription());
    assertEquals("{\"type\":\"object\"}", td.getParameters());
  }

  @Test
  void nullOptionalFields() {
    ToolDefinition td = ToolDefinition.builder().name("f").build();
    assertEquals("f", td.getName());
    assertNull(td.getDescription());
    assertNull(td.getParameters());
  }
}
