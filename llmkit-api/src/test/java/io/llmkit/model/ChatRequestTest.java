package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ChatRequestTest {

  @Test
  void builderBasicFields() {
    ChatRequest req =
        ChatRequest.builder()
            .model("gpt-4o")
            .addMessage(ChatMessage.user("hello"))
            .temperature(0.7)
            .maxTokens(100)
            .build();

    assertEquals("gpt-4o", req.getModel());
    assertEquals(1, req.getMessages().size());
    assertEquals(0.7, req.getTemperature());
    assertEquals(100, req.getMaxTokens());
    assertTrue(req.getTools().isEmpty());
  }

  @Test
  void messagesListIsImmutable() {
    ChatRequest req =
        ChatRequest.builder()
            .addMessage(ChatMessage.user("a"))
            .addMessage(ChatMessage.user("b"))
            .build();
    assertEquals(2, req.getMessages().size());
    assertThrows(
        UnsupportedOperationException.class, () -> req.getMessages().add(ChatMessage.user("c")));
  }

  @Test
  void messagesSetterReplaces() {
    ChatRequest req =
        ChatRequest.builder()
            .addMessage(ChatMessage.user("old"))
            .messages(Collections.singletonList(ChatMessage.user("new")))
            .build();
    assertEquals(1, req.getMessages().size());
    assertEquals("new", req.getMessages().get(0).getContent());
  }

  @Test
  void toolsAreImmutable() {
    ToolDefinition td = ToolDefinition.builder().name("f").build();
    ChatRequest req = ChatRequest.builder().tools(Arrays.asList(td)).build();
    assertThrows(UnsupportedOperationException.class, () -> req.getTools().add(null));
  }

  @Test
  void nullFieldsDefault() {
    ChatRequest req = ChatRequest.builder().build();
    assertNull(req.getModel());
    assertNull(req.getTemperature());
    assertNull(req.getMaxTokens());
    assertTrue(req.getMessages().isEmpty());
    assertTrue(req.getTools().isEmpty());
  }
}
