package io.llmkit.kimi;

import io.llmkit.LlmClient;
import io.llmkit.openai.AbstractOpenAiClientBuilder;

/** Builder for Kimi (Moonshot AI) client instances. */
public final class KimiClientBuilder extends AbstractOpenAiClientBuilder<KimiClientBuilder> {

  private static final String DEFAULT_MODEL = "moonshot-v1-8k";

  public KimiClientBuilder() {
    this.model = DEFAULT_MODEL;
  }

  @Override
  protected KimiClientBuilder self() {
    return this;
  }

  @Override
  protected LlmClient doBuild() {
    return new KimiClient(this);
  }
}
