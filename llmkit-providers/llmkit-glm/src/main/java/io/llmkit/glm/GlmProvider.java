package io.llmkit.glm;

import io.llmkit.LlmClientBuilder;
import io.llmkit.LlmProvider;

/** SPI registration for the GLM (Zhipu AI) provider. */
public final class GlmProvider implements LlmProvider {

  @Override
  public String name() {
    return "glm";
  }

  @Override
  public LlmClientBuilder createBuilder() {
    return new GlmClientBuilder();
  }
}
