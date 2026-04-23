# llmkit

[![CI](https://github.com/IntelliMill/llmkit/actions/workflows/ci.yml/badge.svg)](https://github.com/IntelliMill/llmkit/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-blue.svg)](https://adoptium.net/)
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-Zero-green.svg)]()

[English](README.md) | **[中文](README.zh-CN.md)** | [日本語](README.ja.md) | [한국어](README.ko.md)

轻量级、零依赖、多 Provider 的 Java LLM 客户端库。

**JDK 8+ | 零框架依赖 | 3 行代码 Hello World**

## 为什么选择 llmkit？

Java AI 生态目前只有两个主流选择：**Spring AI**（强耦合 Spring Boot，130+ 模块）和 **Langchain4j**（功能膨胀，90+ 模块）。两者都要求 JDK 17+。对于只需要"调用 LLM API"的开发者来说，太重了。

**llmkit 的定位是 LLM 客户端领域的 okhttp** —— 轻量、零依赖、多 Provider。

| 特性 | llmkit | Spring AI | Langchain4j |
|------|--------|-----------|-------------|
| JDK 基线 | **8** | 17 | 17 |
| 框架依赖 | **无** | Spring Boot | 无 |
| 模块数量 | **5** | 130+ | 90+ |
| 外部依赖 | **0** | 大量 | 大量 |
| Hello World 代码行数 | **3** | 10+ | 5+ |
| JAR 大小（核心） | **< 200KB** | ~50MB | ~10MB |

## 快速开始

### 1. 添加依赖

```xml
<!-- 核心 API + OpenAI Provider -->
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

### 2. Hello World（3 行代码）

```java
import io.llmkit.LlmClient;
import io.llmkit.LlmKit;
import io.llmkit.Providers;

LlmClient client = LlmKit.create("sk-xxx");
String answer = client.chat("用一句话解释量子计算");
System.out.println(answer);
```

## 使用方式

### 切换 Provider

```java
// Anthropic
LlmClient anthropic = LlmKit.builder(Providers.ANTHROPIC)
    .apiKey("sk-ant-xxx")
    .model("claude-sonnet-4-20250514")
    .build();
```

### 流式输出

```java
client.chatStream(
    ChatRequest.builder()
        .addMessage(ChatMessage.user("写一首诗"))
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

### 自定义端点（DeepSeek、Ollama、Moonshot 等）

```java
LlmClient deepseek = LlmKit.builder(Providers.OPENAI)
    .apiKey("sk-xxx")
    .baseUrl("https://api.deepseek.com/v1")
    .model("deepseek-chat")
    .build();
```

### 完整配置

```java
LlmClient client = LlmKit.builder(Providers.OPENAI)
    .apiKey("sk-xxx")
    .model("gpt-4o")
    .baseUrl("https://api.openai.com")       // 可选
    .timeout(Duration.ofSeconds(60))          // 可选
    .retry(RetryPolicy.builder()              // 可选
        .maxRetries(3)
        .initialDelayMs(1000)
        .backoffMultiplier(2.0)
        .build())
    .build();
```

### 函数调用 / 工具使用

```java
ToolDefinition weatherTool = ToolDefinition.builder()
    .name("get_weather")
    .description("获取指定城市的当前天气")
    .parameters("{\"type\":\"object\",\"properties\":{\"location\":{\"type\":\"string\"}},\"required\":[\"location\"]}")
    .build();

ChatResponse response = client.chat(ChatRequest.builder()
    .addMessage(ChatMessage.user("东京现在天气怎么样？"))
    .tools(Collections.singletonList(weatherTool))
    .build());

// 检查模型是否希望调用工具
if (response.content() == null && !response.getChoices().get(0).getMessage().getToolCalls().isEmpty()) {
    ToolCall call = response.getChoices().get(0).getMessage().getToolCalls().get(0);
    // 执行你的函数，然后将结果发回
}
```

## 架构

```
llmkit-api       核心 API（零依赖）
llmkit-core      HTTP、SSE、JSON、重试逻辑
llmkit-openai    OpenAI Provider（Chat Completions API）
llmkit-anthropic Anthropic Provider（Messages API）
llmkit-examples  示例代码
```

### 设计原则

- **零依赖**：`llmkit-api` 不引入任何第三方库
- **手写 JSON**：不使用 Jackson/Gson —— 仅处理固定的 OpenAI/Anthropic 格式
- **JDK HttpURLConnection**：不需要 OkHttp/Apache HttpClient
- **Java ServiceLoader**：标准 SPI 机制实现 Provider 发现
- **不可变模型**：所有模型类均为 `final`，使用 Builder 模式，不依赖 Lombok

## 构建

```bash
mvn clean compile       # 编译
mvn spotless:apply      # 格式化代码
mvn spotless:check      # 检查格式（CI）
```

## 许可证

[MIT](LICENSE)
