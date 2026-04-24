package io.llmkit.deepseek;

import io.llmkit.LlmClientBuilder;
import io.llmkit.LlmProvider;

/** SPI registration for the DeepSeek provider. */
public final class DeepSeekProvider implements LlmProvider {

  @Override
  public String name() {
    return "deepseek";
  }

  @Override
  public LlmClientBuilder createBuilder() {
    return new DeepSeekClientBuilder();
  }
}
