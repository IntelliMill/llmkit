package io.llmkit;

import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatResponse;

/** Callback interface for streaming responses. */
public interface StreamListener {

  /** Called when a chunk is received. */
  void onChunk(ChatChunk chunk);

  /** Called when the stream completes with the full response. */
  void onComplete(ChatResponse response);

  /** Called when an error occurs. */
  void onError(Throwable error);
}
