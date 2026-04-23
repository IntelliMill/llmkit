package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ChatResponseTest {

  @Test
  void contentConvenienceMethod() {
    ChatMessage msg = ChatMessage.assistant("hello world");
    ChatResponse.Choice choice = new ChatResponse.Choice(0, msg, "stop");
    ChatResponse resp =
        new ChatResponse("id-1", Arrays.asList(choice), new Usage(10, 20, 30), "gpt-4o");

    assertEquals("hello world", resp.content());
    assertEquals("id-1", resp.getId());
    assertEquals("gpt-4o", resp.getModel());
    assertEquals(10, resp.getUsage().getPromptTokens());
    assertEquals(20, resp.getUsage().getCompletionTokens());
    assertEquals(30, resp.getUsage().getTotalTokens());
  }

  @Test
  void contentReturnsNullWhenNoChoices() {
    ChatResponse resp =
        new ChatResponse("id", Collections.<ChatResponse.Choice>emptyList(), null, "m");
    assertNull(resp.content());
  }

  @Test
  void choicesAreImmutable() {
    ChatResponse.Choice c = new ChatResponse.Choice(0, ChatMessage.assistant("x"), "stop");
    ChatResponse resp = new ChatResponse("id", Arrays.asList(c), null, "m");
    assertThrows(UnsupportedOperationException.class, () -> resp.getChoices().add(null));
  }

  @Test
  void choiceFields() {
    ChatMessage msg = ChatMessage.assistant("test");
    ChatResponse.Choice choice = new ChatResponse.Choice(2, msg, "length");
    assertEquals(2, choice.getIndex());
    assertEquals("test", choice.getMessage().getContent());
    assertEquals("length", choice.getFinishReason());
  }
}
