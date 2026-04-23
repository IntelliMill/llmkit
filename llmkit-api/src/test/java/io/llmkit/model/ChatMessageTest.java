package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ChatMessageTest {

  @Test
  void systemFactory() {
    ChatMessage msg = ChatMessage.system("you are a bot");
    assertEquals(ChatMessage.Role.SYSTEM, msg.getRole());
    assertEquals("you are a bot", msg.getContent());
    assertTrue(msg.getToolCalls().isEmpty());
    assertNull(msg.getToolCallId());
  }

  @Test
  void userFactory() {
    ChatMessage msg = ChatMessage.user("hello");
    assertEquals(ChatMessage.Role.USER, msg.getRole());
    assertEquals("hello", msg.getContent());
  }

  @Test
  void assistantFactory() {
    ChatMessage msg = ChatMessage.assistant("hi there");
    assertEquals(ChatMessage.Role.ASSISTANT, msg.getRole());
    assertEquals("hi there", msg.getContent());
  }

  @Test
  void toolResultFactory() {
    ChatMessage msg = ChatMessage.toolResult("call-123", "result data");
    assertEquals(ChatMessage.Role.TOOL, msg.getRole());
    assertEquals("call-123", msg.getToolCallId());
    assertEquals("result data", msg.getContent());
  }

  @Test
  void builderWithToolCalls() {
    ToolCall tc = new ToolCall("id-1", "func_a", "{\"x\":1}");
    ChatMessage msg =
        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).toolCalls(Arrays.asList(tc)).build();
    assertEquals(1, msg.getToolCalls().size());
    assertEquals("id-1", msg.getToolCalls().get(0).getId());
    assertEquals("func_a", msg.getToolCalls().get(0).getName());
    assertEquals("{\"x\":1}", msg.getToolCalls().get(0).getArguments());
  }

  @Test
  void toolCallsListIsImmutable() {
    ToolCall tc = new ToolCall("id-1", "f", "{}");
    ChatMessage msg =
        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).toolCalls(Arrays.asList(tc)).build();
    assertThrows(
        UnsupportedOperationException.class,
        () -> msg.getToolCalls().add(new ToolCall("x", "y", "{}")));
  }

  @Test
  void defaultToolCallsIsEmpty() {
    ChatMessage msg = ChatMessage.user("hi");
    assertTrue(msg.getToolCalls().isEmpty());
  }

  @Test
  void nullContentAllowed() {
    ChatMessage msg = ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).build();
    assertNull(msg.getContent());
  }
}
