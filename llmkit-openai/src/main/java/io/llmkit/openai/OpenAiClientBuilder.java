package io.llmkit.openai;

import io.llmkit.LlmClient;
import io.llmkit.internal.AbstractLlmClientBuilder;

/** Builder for OpenAI client instances. */
public final class OpenAiClientBuilder extends AbstractLlmClientBuilder<OpenAiClientBuilder> {

  private static final String DEFAULT_MODEL = "gpt-4o";

  public OpenAiClientBuilder() {
    this.model = DEFAULT_MODEL;
  }

  @Override
  protected OpenAiClientBuilder self() {
    return this;
  }

  @Override
  protected LlmClient doBuild() {
    return new OpenAiClient(this);
  }
}
