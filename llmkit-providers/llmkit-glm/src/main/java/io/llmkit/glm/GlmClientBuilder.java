package io.llmkit.glm;

import io.llmkit.LlmClient;
import io.llmkit.openai.AbstractOpenAiClientBuilder;

/** Builder for GLM (Zhipu AI) client instances. */
public final class GlmClientBuilder extends AbstractOpenAiClientBuilder<GlmClientBuilder> {

  private static final String DEFAULT_MODEL = "glm-4";

  public GlmClientBuilder() {
    this.model = DEFAULT_MODEL;
  }

  @Override
  protected GlmClientBuilder self() {
    return this;
  }

  @Override
  protected LlmClient doBuild() {
    return new GlmClient(this);
  }
}
