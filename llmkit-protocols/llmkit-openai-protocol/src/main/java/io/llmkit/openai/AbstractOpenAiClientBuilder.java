package io.llmkit.openai;

import io.llmkit.internal.AbstractLlmClientBuilder;

/**
 * Abstract builder for OpenAI-compatible API clients.
 *
 * <p>Subclasses should set a default model in their constructor and implement {@link #self()} and
 * {@link #doBuild()}.
 */
public abstract class AbstractOpenAiClientBuilder<T extends AbstractOpenAiClientBuilder<T>>
    extends AbstractLlmClientBuilder<T> {}
