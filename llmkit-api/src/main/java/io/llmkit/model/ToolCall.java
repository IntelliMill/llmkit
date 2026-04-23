package io.llmkit.model;

/** A tool/function call within an assistant message. */
public final class ToolCall {

  private final String id;
  private final String name;
  private final String arguments;

  public ToolCall(String id, String name, String arguments) {
    this.id = id;
    this.name = name;
    this.arguments = arguments;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getArguments() {
    return arguments;
  }
}
