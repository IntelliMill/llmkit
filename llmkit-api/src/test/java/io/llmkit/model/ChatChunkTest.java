package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ChatChunkTest {

  @Test
  void deltaConvenienceMethod() {
    ChatMessage delta =
        ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content("Hello").build();
    ChatChunk.ChunkChoice cc = new ChatChunk.ChunkChoice(0, delta, null);
    ChatChunk chunk = new ChatChunk("chatcmpl-123", Arrays.asList(cc));

    assertEquals("Hello", chunk.delta());
    assertEquals("chatcmpl-123", chunk.getId());
    assertEquals(1, chunk.getChoices().size());
  }

  @Test
  void deltaReturnsNullWhenNoChoices() {
    ChatChunk chunk = new ChatChunk("id", Collections.<ChatChunk.ChunkChoice>emptyList());
    assertNull(chunk.delta());
  }

  @Test
  void deltaReturnsNullWhenDeltaContentIsNull() {
    ChatMessage delta = ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).build();
    ChatChunk.ChunkChoice cc = new ChatChunk.ChunkChoice(0, delta, "stop");
    ChatChunk chunk = new ChatChunk("id", Arrays.asList(cc));
    assertNull(chunk.delta());
    assertEquals("stop", chunk.getChoices().get(0).getFinishReason());
  }

  @Test
  void choicesAreImmutable() {
    ChatChunk.ChunkChoice cc = new ChatChunk.ChunkChoice(0, null, null);
    ChatChunk chunk = new ChatChunk("id", Arrays.asList(cc));
    assertThrows(UnsupportedOperationException.class, () -> chunk.getChoices().add(null));
  }
}
