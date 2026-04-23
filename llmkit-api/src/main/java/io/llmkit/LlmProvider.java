package io.llmkit;

/** SPI interface for LLM providers, discovered via Java ServiceLoader. */
public interface LlmProvider {

  /** Provider name, e.g. "openai", "anthropic". */
  String name();

  /** Create a new client builder for this provider. */
  LlmClientBuilder createBuilder();
}
