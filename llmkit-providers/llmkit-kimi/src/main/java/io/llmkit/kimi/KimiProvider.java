package io.llmkit.kimi;

import io.llmkit.LlmClientBuilder;
import io.llmkit.LlmProvider;

/** SPI registration for the Kimi (Moonshot AI) provider. */
public final class KimiProvider implements LlmProvider {

  @Override
  public String name() {
    return "kimi";
  }

  @Override
  public LlmClientBuilder createBuilder() {
    return new KimiClientBuilder();
  }
}
