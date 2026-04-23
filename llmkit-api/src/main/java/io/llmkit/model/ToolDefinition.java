package io.llmkit.model;

/** Definition of a tool/function that the model can invoke. */
public final class ToolDefinition {

  private final String name;
  private final String description;
  private final String parameters;

  private ToolDefinition(Builder builder) {
    this.name = builder.name;
    this.description = builder.description;
    this.parameters = builder.parameters;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getParameters() {
    return parameters;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private String name;
    private String description;
    private String parameters;

    private Builder() {}

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder parameters(String parameters) {
      this.parameters = parameters;
      return this;
    }

    public ToolDefinition build() {
      return new ToolDefinition(this);
    }
  }
}
