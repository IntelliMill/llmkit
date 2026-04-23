package io.llmkit.model;

/** The result of executing a tool call. */
public final class ToolResult {

  private final String toolCallId;
  private final String content;
  private final boolean isError;

  public ToolResult(String toolCallId, String content, boolean isError) {
    this.toolCallId = toolCallId;
    this.content = content;
    this.isError = isError;
  }

  public ToolResult(String toolCallId, String content) {
    this(toolCallId, content, false);
  }

  public String getToolCallId() {
    return toolCallId;
  }

  public String getContent() {
    return content;
  }

  public boolean isError() {
    return isError;
  }
}
