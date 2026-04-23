package io.llmkit.anthropic;

import io.llmkit.LlmClientBuilder;
import io.llmkit.LlmProvider;

/** SPI registration for the Anthropic provider. */
public final class AnthropicProvider implements LlmProvider {

  @Override
  public String name() {
    return "anthropic";
  }

  @Override
  public LlmClientBuilder createBuilder() {
    return new AnthropicClientBuilder();
  }
}
