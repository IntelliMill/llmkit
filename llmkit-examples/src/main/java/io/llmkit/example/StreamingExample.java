package io.llmkit.example;

import io.llmkit.LlmClient;
import io.llmkit.LlmKit;
import io.llmkit.Providers;
import io.llmkit.StreamListener;
import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatMessage;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;

/** Streaming output example. */
public class StreamingExample {

  public static void main(String[] args) {
    LlmClient client = LlmKit.builder(Providers.OPENAI).apiKey("sk-xxx").model("gpt-4o").build();

    ChatRequest request =
        ChatRequest.builder()
            .addMessage(ChatMessage.user("Write a short poem about spring"))
            .build();

    client.chatStream(
        request,
        new StreamListener() {
          @Override
          public void onChunk(ChatChunk chunk) {
            String delta = chunk.delta();
            if (delta != null) {
              System.out.print(delta);
            }
          }

          @Override
          public void onComplete(ChatResponse response) {
            System.out.println("\n--- Stream complete ---");
          }

          @Override
          public void onError(Throwable error) {
            error.printStackTrace();
          }
        });

    client.close();
  }
}
