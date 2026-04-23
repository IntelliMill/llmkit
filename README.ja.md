# llmkit

[![CI](https://github.com/IntelliMill/llmkit/actions/workflows/CI/badge.svg)](https://github.com/IntelliMill/llmkit/actions/workflows/CI)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-blue.svg)](https://adoptium.net/)
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-Zero-green.svg)]()

[English](README.md) | [中文](README.zh-CN.md) | **[日本語](README.ja.md)** | [한국어](README.ko.md)

軽量・ゼロ依存・マルチプロバイダの Java LLM クライアントライブラリ。

**JDK 8+ | フレームワーク依存なし | 3行で Hello World**

## なぜ llmkit を選ぶのか？

Java AI エコシステムには現在、**Spring AI**（Spring Boot と強く結合、130+ モジュール）と **Langchain4j**（機能の肥大化、90+ モジュール）の 2 つの主流な選択肢しかありません。どちらも JDK 17+ が必要です。「LLM API を呼び出すだけ」の開発者にとっては重すぎます。

**llmkit は LLM クライアントにおける okhttp のような存在** —— 軽量、ゼロ依存、マルチプロバイダ。

| 機能 | llmkit | Spring AI | Langchain4j |
|------|--------|-----------|-------------|
| JDK ベースライン | **8** | 17 | 17 |
| フレームワーク依存 | **なし** | Spring Boot | なし |
| モジュール数 | **5** | 130+ | 90+ |
| 外部依存 | **0** | 多数 | 多数 |
| Hello World の行数 | **3** | 10+ | 5+ |
| JAR サイズ（コア） | **< 200KB** | ~50MB | ~10MB |

## クイックスタート

### 1. 依存関係の追加

```xml
<!-- コア API + OpenAI プロバイダ -->
<dependency>
    <groupId>com.github.intellimill</groupId>
    <artifactId>llmkit-api</artifactId>
    <version>0.1.0</version>
</dependency>
<dependency>
    <groupId>com.github.intellimill</groupId>
    <artifactId>llmkit-openai</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. Hello World（3行）

```java
import io.llmkit.LlmClient;
import io.llmkit.LlmKit;
import io.llmkit.Providers;

LlmClient client = LlmKit.create("sk-xxx");
String answer = client.chat("量子コンピューティングを一言で説明してください");
System.out.println(answer);
```

## 使い方

### プロバイダの切り替え

```java
// Anthropic
LlmClient anthropic = LlmKit.builder(Providers.ANTHROPIC)
    .apiKey("sk-ant-xxx")
    .model("claude-sonnet-4-20250514")
    .build();
```

### ストリーミング

```java
client.chatStream(
    ChatRequest.builder()
        .addMessage(ChatMessage.user("詩を書いてください"))
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

### カスタムエンドポイント（DeepSeek、Ollama、Moonshot など）

```java
LlmClient deepseek = LlmKit.builder(Providers.OPENAI)
    .apiKey("sk-xxx")
    .baseUrl("https://api.deepseek.com/v1")
    .model("deepseek-chat")
    .build();
```

### フル設定

```java
LlmClient client = LlmKit.builder(Providers.OPENAI)
    .apiKey("sk-xxx")
    .model("gpt-4o")
    .baseUrl("https://api.openai.com")       // オプション
    .timeout(Duration.ofSeconds(60))          // オプション
    .retry(RetryPolicy.builder()              // オプション
        .maxRetries(3)
        .initialDelayMs(1000)
        .backoffMultiplier(2.0)
        .build())
    .build();
```

### 関数呼び出し / ツール使用

```java
ToolDefinition weatherTool = ToolDefinition.builder()
    .name("get_weather")
    .description("指定された場所の現在の天気を取得する")
    .parameters("{\"type\":\"object\",\"properties\":{\"location\":{\"type\":\"string\"}},\"required\":[\"location\"]}")
    .build();

ChatResponse response = client.chat(ChatRequest.builder()
    .addMessage(ChatMessage.user("東京の天気はどうですか？"))
    .tools(Collections.singletonList(weatherTool))
    .build());

// モデルがツールを呼び出したいか確認
if (response.content() == null && !response.getChoices().get(0).getMessage().getToolCalls().isEmpty()) {
    ToolCall call = response.getChoices().get(0).getMessage().getToolCalls().get(0);
    // 関数を実行し、結果を返信する
}
```

## アーキテクチャ

```
llmkit-api       コア API（ゼロ依存）
llmkit-core      HTTP、SSE、JSON、リトライロジック
llmkit-openai    OpenAI プロバイダ（Chat Completions API）
llmkit-anthropic Anthropic プロバイダ（Messages API）
llmkit-examples  サンプルコード
```

### 設計原則

- **ゼロ依存**：`llmkit-api` はサードパーティライブラリを一切使用しません
- **手書き JSON**：Jackson/Gson 不使用 —— OpenAI/Anthropic の固定フォーマットのみ処理
- **JDK HttpURLConnection**：OkHttp/Apache HttpClient 不要
- **Java ServiceLoader**：標準 SPI メカニズムによるプロバイダ検出
- **不変モデル**：すべてのモデルクラスは `final` で Builder パターンを使用、Lombok 不使用

## ビルド

```bash
mvn clean compile       # コンパイル
mvn spotless:apply      # コードフォーマット
mvn spotless:check      # フォーマットチェック（CI）
```

## ライセンス

[MIT](LICENSE)
