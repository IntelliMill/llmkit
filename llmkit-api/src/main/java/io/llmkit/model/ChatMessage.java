package io.llmkit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An immutable chat message with a role and content. */
public final class ChatMessage {

  private final Role role;
  private final String content;
  private final List<ToolCall> toolCalls;
  private final String toolCallId;

  public enum Role {
    SYSTEM,
    USER,
    ASSISTANT,
    TOOL
  }

  private ChatMessage(Builder builder) {
    this.role = builder.role;
    this.content = builder.content;
    this.toolCalls =
        builder.toolCalls != null
            ? Collections.unmodifiableList(new ArrayList<>(builder.toolCalls))
            : Collections.<ToolCall>emptyList();
    this.toolCallId = builder.toolCallId;
  }

  public Role getRole() {
    return role;
  }

  public String getContent() {
    return content;
  }

  public List<ToolCall> getToolCalls() {
    return toolCalls;
  }

  public String getToolCallId() {
    return toolCallId;
  }

  public static ChatMessage system(String content) {
    return new Builder().role(Role.SYSTEM).content(content).build();
  }

  public static ChatMessage user(String content) {
    return new Builder().role(Role.USER).content(content).build();
  }

  public static ChatMessage assistant(String content) {
    return new Builder().role(Role.ASSISTANT).content(content).build();
  }

  public static ChatMessage toolResult(String toolCallId, String content) {
    return new Builder().role(Role.TOOL).content(content).toolCallId(toolCallId).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private Role role;
    private String content;
    private List<ToolCall> toolCalls;
    private String toolCallId;

    private Builder() {}

    public Builder role(Role role) {
      this.role = role;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }

    public Builder toolCalls(List<ToolCall> toolCalls) {
      this.toolCalls = toolCalls;
      return this;
    }

    public Builder toolCallId(String toolCallId) {
      this.toolCallId = toolCallId;
      return this;
    }

    public ChatMessage build() {
      return new ChatMessage(this);
    }
  }
}
