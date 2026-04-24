package io.llmkit.openai;

import io.llmkit.LlmClientBuilder;
import io.llmkit.LlmProvider;

/** SPI registration for the OpenAI provider. */
public final class OpenAiProvider implements LlmProvider {

  @Override
  public String name() {
    return "openai";
  }

  @Override
  public LlmClientBuilder createBuilder() {
    return new OpenAiClientBuilder();
  }
}
