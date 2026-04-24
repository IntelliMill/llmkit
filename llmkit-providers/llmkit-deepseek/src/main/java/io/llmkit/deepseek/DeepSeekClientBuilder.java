package io.llmkit.deepseek;

import io.llmkit.LlmClient;
import io.llmkit.openai.AbstractOpenAiClientBuilder;

/** Builder for DeepSeek client instances. */
public final class DeepSeekClientBuilder
    extends AbstractOpenAiClientBuilder<DeepSeekClientBuilder> {

  private static final String DEFAULT_MODEL = "deepseek-chat";

  public DeepSeekClientBuilder() {
    this.model = DEFAULT_MODEL;
  }

  @Override
  protected DeepSeekClientBuilder self() {
    return this;
  }

  @Override
  protected LlmClient doBuild() {
    return new DeepSeekClient(this);
  }
}
