package io.llmkit.minimax;

import io.llmkit.LlmClient;
import io.llmkit.openai.AbstractOpenAiClientBuilder;

/** Builder for MiniMax client instances. */
public final class MiniMaxClientBuilder extends AbstractOpenAiClientBuilder<MiniMaxClientBuilder> {

  private static final String DEFAULT_MODEL = "MiniMax-Text-01";

  public MiniMaxClientBuilder() {
    this.model = DEFAULT_MODEL;
  }

  @Override
  protected MiniMaxClientBuilder self() {
    return this;
  }

  @Override
  protected LlmClient doBuild() {
    return new MiniMaxClient(this);
  }
}
