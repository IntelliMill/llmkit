package io.llmkit.example;

import io.llmkit.LlmClient;
import io.llmkit.LlmKit;

/** 3-line Hello World example. */
public class QuickStart {

  public static void main(String[] args) {
    LlmClient client = LlmKit.create("sk-xxx");
    String answer = client.chat("Explain quantum computing in one sentence");
    System.out.println(answer);
    client.close();
  }
}
