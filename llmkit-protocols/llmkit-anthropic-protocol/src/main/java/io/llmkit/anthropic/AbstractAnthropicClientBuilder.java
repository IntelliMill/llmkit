package io.llmkit.anthropic;

import io.llmkit.internal.AbstractLlmClientBuilder;

/**
 * Abstract builder for Anthropic-compatible API clients.
 *
 * <p>Subclasses should set a default model in their constructor and implement {@link #self()} and
 * {@link #doBuild()}.
 */
public abstract class AbstractAnthropicClientBuilder<T extends AbstractAnthropicClientBuilder<T>>
    extends AbstractLlmClientBuilder<T> {}
