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
  private final Double topP;
  private final Integer seed;
  private final List<String> stop;
  private final Boolean logprobs;
  private final Integer topLogprobs;
  private final String responseFormat;

  private ChatRequest(Builder builder) {
    this.model = builder.model;
    this.messages = Collections.unmodifiableList(new ArrayList<>(builder.messages));
    this.temperature = builder.temperature;
    this.maxTokens = builder.maxTokens;
    this.tools =
        builder.tools != null
            ? Collections.unmodifiableList(new ArrayList<>(builder.tools))
            : Collections.<ToolDefinition>emptyList();
    this.topP = builder.topP;
    this.seed = builder.seed;
    this.stop =
        builder.stop != null
            ? Collections.unmodifiableList(new ArrayList<>(builder.stop))
            : Collections.<String>emptyList();
    this.logprobs = builder.logprobs;
    this.topLogprobs = builder.topLogprobs;
    this.responseFormat = builder.responseFormat;
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

  public Double getTopP() {
    return topP;
  }

  public Integer getSeed() {
    return seed;
  }

  public List<String> getStop() {
    return stop;
  }

  public Boolean getLogprobs() {
    return logprobs;
  }

  public Integer getTopLogprobs() {
    return topLogprobs;
  }

  public String getResponseFormat() {
    return responseFormat;
  }

  /** Create a {@code response_format} value for JSON mode. */
  public static String jsonResponseFormat() {
    return "{\"type\":\"json_object\"}";
  }

  /** Create a {@code response_format} value for text mode. */
  public static String textResponseFormat() {
    return "{\"type\":\"text\"}";
  }

  /**
   * Create a {@code response_format} value for structured output with a JSON schema.
   *
   * @param name a name for this response format
   * @param jsonSchema the JSON schema string
   */
  public static String schemaResponseFormat(String name, String jsonSchema) {
    return "{\"type\":\"json_schema\",\"json_schema\":{\"name\":\""
        + name
        + "\",\"strict\":true,\"schema\":"
        + jsonSchema
        + "}}";
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
    private Double topP;
    private Integer seed;
    private List<String> stop;
    private Boolean logprobs;
    private Integer topLogprobs;
    private String responseFormat;

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

    public Builder topP(Double topP) {
      this.topP = topP;
      return this;
    }

    public Builder seed(Integer seed) {
      this.seed = seed;
      return this;
    }

    public Builder stop(List<String> stop) {
      this.stop = stop;
      return this;
    }

    public Builder logprobs(Boolean logprobs) {
      this.logprobs = logprobs;
      return this;
    }

    public Builder topLogprobs(Integer topLogprobs) {
      this.topLogprobs = topLogprobs;
      return this;
    }

    public Builder responseFormat(String responseFormat) {
      this.responseFormat = responseFormat;
      return this;
    }

    public ChatRequest build() {
      return new ChatRequest(this);
    }
  }
}
