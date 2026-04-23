package io.llmkit;

/**
 * Constants for built-in provider names. Use these instead of raw strings when calling {@link
 * LlmKit#builder(String)}.
 */
public final class Providers {

  private Providers() {}

  public static final String OPENAI = "openai";
  public static final String ANTHROPIC = "anthropic";
}
