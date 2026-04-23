package io.llmkit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An immutable streaming chunk. */
public final class ChatChunk {

  private final String id;
  private final List<ChunkChoice> choices;

  public ChatChunk(String id, List<ChunkChoice> choices) {
    this.id = id;
    this.choices = Collections.unmodifiableList(new ArrayList<>(choices));
  }

  public String getId() {
    return id;
  }

  public List<ChunkChoice> getChoices() {
    return choices;
  }

  /** Convenience method: get the delta text of the first choice. */
  public String delta() {
    if (choices.isEmpty()) {
      return null;
    }
    ChatMessage delta = choices.get(0).getDelta();
    return delta != null ? delta.getContent() : null;
  }

  /** A single choice in a streaming chunk. */
  public static final class ChunkChoice {
    private final int index;
    private final ChatMessage delta;
    private final String finishReason;

    public ChunkChoice(int index, ChatMessage delta, String finishReason) {
      this.index = index;
      this.delta = delta;
      this.finishReason = finishReason;
    }

    public int getIndex() {
      return index;
    }

    public ChatMessage getDelta() {
      return delta;
    }

    public String getFinishReason() {
      return finishReason;
    }
  }
}
