package io.llmkit;

import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import java.util.Collections;

/** The core LLM client interface. */
public interface LlmClient extends AutoCloseable {

  /** Synchronous chat completion. */
  ChatResponse chat(ChatRequest request);

  /** Streaming chat with callback. */
  void chatStream(ChatRequest request, StreamListener listener);

  /** One-shot chat with the default model. */
  default String chat(String message) {
    ChatRequest request =
        ChatRequest.builder()
            .messages(Collections.singletonList(io.llmkit.model.ChatMessage.user(message)))
            .build();
    return chat(request).content();
  }

  @Override
  default void close() {}
}
