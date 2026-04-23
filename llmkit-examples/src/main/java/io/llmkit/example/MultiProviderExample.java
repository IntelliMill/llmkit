package io.llmkit.example;

import io.llmkit.LlmClient;
import io.llmkit.LlmKit;
import io.llmkit.Providers;
import io.llmkit.model.ChatMessage;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;

/** Multi-provider switching example. */
public class MultiProviderExample {

  public static void main(String[] args) {
    // List available providers
    System.out.println("Available providers: " + LlmKit.providers());

    // Use OpenAI
    LlmClient openai = LlmKit.builder(Providers.OPENAI).apiKey("sk-xxx").model("gpt-4o").build();

    ChatResponse response =
        openai.chat(
            ChatRequest.builder().addMessage(ChatMessage.user("Hello from OpenAI!")).build());
    System.out.println("OpenAI: " + response.content());

    // Switch to Anthropic
    LlmClient anthropic =
        LlmKit.builder(Providers.ANTHROPIC)
            .apiKey("sk-ant-xxx")
            .model("claude-sonnet-4-20250514")
            .build();

    response =
        anthropic.chat(
            ChatRequest.builder().addMessage(ChatMessage.user("Hello from Anthropic!")).build());
    System.out.println("Anthropic: " + response.content());

    // Use a custom OpenAI-compatible endpoint (e.g. DeepSeek)
    LlmClient deepseek =
        LlmKit.builder(Providers.OPENAI)
            .apiKey("sk-xxx")
            .baseUrl("https://api.deepseek.com/v1")
            .model("deepseek-chat")
            .build();

    response =
        deepseek.chat(
            ChatRequest.builder().addMessage(ChatMessage.user("Hello from DeepSeek!")).build());
    System.out.println("DeepSeek: " + response.content());

    openai.close();
    anthropic.close();
    deepseek.close();
  }
}
