package io.llmkit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** An immutable chat request built via the Builder pattern. */
public final class ChatRequest {

  private final String model;
  private final List<ChatMessage> messages;
  private final Double temperature;
  private final Integer maxTokens;
  private final List<ToolDefinition> tools;

  private ChatRequest(Builder builder) {
    this.model = builder.model;
    this.messages = Collections.unmodifiableList(new ArrayList<>(builder.messages));
    this.temperature = builder.temperature;
    this.maxTokens = builder.maxTokens;
    this.tools =
        builder.tools != null
            ? Collections.unmodifiableList(new ArrayList<>(builder.tools))
            : Collections.<ToolDefinition>emptyList();
  }

  public String getModel() {
    return model;
  }

  public List<ChatMessage> getMessages() {
    return messages;
  }

  public Double getTemperature() {
    return temperature;
  }

  public Integer getMaxTokens() {
    return maxTokens;
  }

  public List<ToolDefinition> getTools() {
    return tools;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String model;
    private final List<ChatMessage> messages = new ArrayList<>();
    private Double temperature;
    private Integer maxTokens;
    private List<ToolDefinition> tools;

    private Builder() {}

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder messages(List<ChatMessage> messages) {
      this.messages.clear();
      this.messages.addAll(messages);
      return this;
    }

    public Builder addMessage(ChatMessage message) {
      this.messages.add(message);
      return this;
    }

    public Builder temperature(Double temperature) {
      this.temperature = temperature;
      return this;
    }

    public Builder maxTokens(Integer maxTokens) {
      this.maxTokens = maxTokens;
      return this;
    }

    public Builder tools(List<ToolDefinition> tools) {
      this.tools = tools;
      return this;
    }

    public ChatRequest build() {
      return new ChatRequest(this);
    }
  }
}
