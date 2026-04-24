package io.llmkit.anthropic;

import io.llmkit.LlmClient;

/** Builder for Anthropic client instances. */
public final class AnthropicClientBuilder
    extends AbstractAnthropicClientBuilder<AnthropicClientBuilder> {

  private static final String DEFAULT_MODEL = "claude-sonnet-4-20250514";

  public AnthropicClientBuilder() {
    this.model = DEFAULT_MODEL;
  }

  @Override
  protected AnthropicClientBuilder self() {
    return this;
  }

  @Override
  protected LlmClient doBuild() {
    return new AnthropicClient(this);
  }
}
