package io.llmkit.qwen;

import io.llmkit.LlmClient;
import io.llmkit.openai.AbstractOpenAiClientBuilder;

/** Builder for Qwen (DashScope) client instances. */
public final class QwenClientBuilder extends AbstractOpenAiClientBuilder<QwenClientBuilder> {

  private static final String DEFAULT_MODEL = "qwen-plus";

  public QwenClientBuilder() {
    this.model = DEFAULT_MODEL;
  }

  @Override
  protected QwenClientBuilder self() {
    return this;
  }

  @Override
  protected LlmClient doBuild() {
    return new QwenClient(this);
  }
}
