package io.llmkit.openai;

import io.llmkit.LlmClient;

/** Builder for OpenAI client instances. */
public final class OpenAiClientBuilder extends AbstractOpenAiClientBuilder<OpenAiClientBuilder> {

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
