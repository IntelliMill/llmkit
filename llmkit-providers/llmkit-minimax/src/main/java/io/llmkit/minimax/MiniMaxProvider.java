package io.llmkit.minimax;

import io.llmkit.LlmClientBuilder;
import io.llmkit.LlmProvider;

/** SPI registration for the MiniMax provider. */
public final class MiniMaxProvider implements LlmProvider {

  @Override
  public String name() {
    return "minimax";
  }

  @Override
  public LlmClientBuilder createBuilder() {
    return new MiniMaxClientBuilder();
  }
}
