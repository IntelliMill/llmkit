package io.llmkit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An immutable chat completion response. */
public final class ChatResponse {

  private final String id;
  private final List<Choice> choices;
  private final Usage usage;
  private final String model;

  public ChatResponse(String id, List<Choice> choices, Usage usage, String model) {
    this.id = id;
    this.choices = Collections.unmodifiableList(new ArrayList<>(choices));
    this.usage = usage;
    this.model = model;
  }

  public String getId() {
    return id;
  }

  public List<Choice> getChoices() {
    return choices;
  }

  public Usage getUsage() {
    return usage;
  }

  public String getModel() {
    return model;
  }

  /** Convenience method: get the text content of the first choice. */
  public String content() {
    if (choices.isEmpty()) {
      return null;
    }
    ChatMessage msg = choices.get(0).getMessage();
    return msg != null ? msg.getContent() : null;
  }

  /** A single choice in the response. */
  public static final class Choice {
    private final int index;
    private final ChatMessage message;
    private final String finishReason;
    private final ChoiceLogprobs logprobs;

    public Choice(int index, ChatMessage message, String finishReason) {
      this(index, message, finishReason, null);
    }

    public Choice(int index, ChatMessage message, String finishReason, ChoiceLogprobs logprobs) {
      this.index = index;
      this.message = message;
      this.finishReason = finishReason;
      this.logprobs = logprobs;
    }

    public int getIndex() {
      return index;
    }

    public ChatMessage getMessage() {
      return message;
    }

    public String getFinishReason() {
      return finishReason;
    }

    public ChoiceLogprobs getLogprobs() {
      return logprobs;
    }
  }
}
