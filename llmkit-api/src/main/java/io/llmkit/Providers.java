package io.llmkit;

/**
 * Constants for built-in provider names. Use these instead of raw strings when calling {@link
 * LlmKit#builder(String)}.
 */
public final class Providers {

  private Providers() {}

  public static final String OPENAI = "openai";
  public static final String ANTHROPIC = "anthropic";
  public static final String DEEPSEEK = "deepseek";
  public static final String GLM = "glm";
  public static final String QWEN = "qwen";
  public static final String MINIMAX = "minimax";
  public static final String KIMI = "kimi";
}
