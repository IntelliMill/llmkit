package io.llmkit.qwen;

import io.llmkit.LlmClientBuilder;
import io.llmkit.LlmProvider;

/** SPI registration for the Qwen (DashScope) provider. */
public final class QwenProvider implements LlmProvider {

  @Override
  public String name() {
    return "qwen";
  }

  @Override
  public LlmClientBuilder createBuilder() {
    return new QwenClientBuilder();
  }
}
