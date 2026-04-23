# llmkit

[![CI](https://github.com/IntelliMill/llmkit/actions/workflows/CI/badge.svg)](https://github.com/IntelliMill/llmkit/actions/workflows/CI)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-blue.svg)](https://adoptium.net/)
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-Zero-green.svg)]()

**[English](README.md)** | [中文](README.zh-CN.md) | [日本語](README.ja.md) | [한국어](README.ko.md)

A lightweight, zero-dependency, multi-provider Java LLM client library.

**JDK 8+ | Zero Framework Dependency | 3-Line Hello World**

## Why llmkit?

Java AI ecosystem only has two mainstream choices: **Spring AI** (strongly coupled to Spring Boot, 130+ modules) and **Langchain4j** (feature bloat, 90+ modules). Both require JDK 17+. For developers who just need to "call an LLM API", they are too heavy.

**llmkit is the okhttp of LLM clients** — lightweight, zero-dependency, multi-provider.

| Feature | llmkit | Spring AI | Langchain4j |
|---------|--------|-----------|-------------|
| JDK Baseline | **8** | 17 | 17 |
| Framework Dependency | **None** | Spring Boot | None |
| Module Count | **5** | 130+ | 90+ |
| External Dependencies | **0** | Many | Many |
| Hello World Lines | **3** | 10+ | 5+ |
| JAR Size (core) | **< 200KB** | ~50MB | ~10MB |

## Quick Start

### 1. Add Dependency

```xml
<!-- Core API + OpenAI provider -->
<dependency>
    <groupId>io.llmkit</groupId>
    <artifactId>llmkit-api</artifactId>
    <version>0.1.0</version>
</dependency>
<dependency>
    <groupId>io.llmkit</groupId>
    <artifactId>llmkit-openai</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. Hello World (3 lines)

```java
import io.llmkit.LlmClient;
import io.llmkit.LlmKit;
import io.llmkit.Providers;

LlmClient client = LlmKit.create("sk-xxx");
String answer = client.chat("Explain quantum computing in one sentence");
System.out.println(answer);
```

## Usage

### Switch Provider

```java
// Anthropic
LlmClient anthropic = LlmKit.builder(Providers.ANTHROPIC)
    .apiKey("sk-ant-xxx")
    .model("claude-sonnet-4-20250514")
    .build();
```

### Streaming

```java
client.chatStream(
    ChatRequest.builder()
        .addMessage(ChatMessage.user("Write a poem"))
        .build(),
    new StreamListener() {
        @Override public void onChunk(ChatChunk chunk) {
            System.out.print(chunk.delta());
        }
        @Override public void onComplete(ChatResponse resp) {}
        @Override public void onError(Throwable e) { e.printStackTrace(); }
    }
);
```

### Custom Endpoint (DeepSeek, Ollama, Moonshot...)

```java
LlmClient deepseek = LlmKit.builder(Providers.OPENAI)
    .apiKey("sk-xxx")
    .baseUrl("https://api.deepseek.com/v1")
    .model("deepseek-chat")
    .build();
```

### Full Configuration

```java
LlmClient client = LlmKit.builder(Providers.OPENAI)
    .apiKey("sk-xxx")
    .model("gpt-4o")
    .baseUrl("https://api.openai.com")       // optional
    .timeout(Duration.ofSeconds(60))          // optional
    .retry(RetryPolicy.builder()              // optional
        .maxRetries(3)
        .initialDelayMs(1000)
        .backoffMultiplier(2.0)
        .build())
    .build();
```

### Function Calling / Tool Use

```java
ToolDefinition weatherTool = ToolDefinition.builder()
    .name("get_weather")
    .description("Get current weather for a location")
    .parameters("{\"type\":\"object\",\"properties\":{\"location\":{\"type\":\"string\"}},\"required\":[\"location\"]}")
    .build();

ChatResponse response = client.chat(ChatRequest.builder()
    .addMessage(ChatMessage.user("What's the weather in Tokyo?"))
    .tools(Collections.singletonList(weatherTool))
    .build());

// Check if model wants to call a tool
if (response.content() == null && !response.getChoices().get(0).getMessage().getToolCalls().isEmpty()) {
    ToolCall call = response.getChoices().get(0).getMessage().getToolCalls().get(0);
    // Execute your function, then send the result back
}
```

## Architecture

```
llmkit-api       Core API (zero dependencies)
llmkit-core      HTTP, SSE, JSON, retry logic
llmkit-openai    OpenAI provider (Chat Completions API)
llmkit-anthropic Anthropic provider (Messages API)
llmkit-examples  Example code
```

### Design Principles

- **Zero dependency**: `llmkit-api` has no third-party dependencies at all
- **Hand-written JSON**: No Jackson/Gson — only handles fixed OpenAI/Anthropic formats
- **JDK HttpURLConnection**: No OkHttp/Apache HttpClient needed
- **Java ServiceLoader**: Standard SPI for provider discovery
- **Immutable models**: All model classes are `final` with Builder pattern, no Lombok

## Building

```bash
mvn clean compile       # Compile
mvn spotless:apply      # Format code
mvn spotless:check      # Check formatting (CI)
```

## License

[MIT](LICENSE)
