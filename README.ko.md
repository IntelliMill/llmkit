# llmkit

[![CI](https://github.com/IntelliMill/llmkit/actions/workflows/CI/badge.svg)](https://github.com/IntelliMill/llmkit/actions/workflows/CI)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 8+](https://img.shields.io/badge/Java-8%2B-blue.svg)](https://adoptium.net/)
[![Zero Dependencies](https://img.shields.io/badge/Dependencies-Zero-green.svg)]()

[English](README.md) | [中文](README.zh-CN.md) | [日本語](README.ja.md) | **[한국어](README.ko.md)**

경량, 제로 의존성, 멀티 프로바이더 Java LLM 클라이언트 라이브러리.

**JDK 8+ | 프레임워크 의존성 없음 | 3줄 Hello World**

## 왜 llmkit인가?

Java AI 생태계에는 현재 **Spring AI**(Spring Boot와 강결합, 130+ 모듈)와 **Langchain4j**(기능 비대, 90+ 모듈) 두 가지 주요 선택지밖에 없습니다. 둘 다 JDK 17+이 필요합니다. "LLM API를 호출하기만 하면 되는" 개발자에게는 너무 무겁습니다.

**llmkit은 LLM 클라이언트 분야의 okhttp** —— 경량, 제로 의존성, 멀티 프로바이더.

| 기능 | llmkit | Spring AI | Langchain4j |
|------|--------|-----------|-------------|
| JDK 기준 | **8** | 17 | 17 |
| 프레임워크 의존 | **없음** | Spring Boot | 없음 |
| 모듈 수 | **5** | 130+ | 90+ |
| 외부 의존성 | **0** | 다수 | 다수 |
| Hello World 줄 수 | **3** | 10+ | 5+ |
| JAR 크기(코어) | **< 200KB** | ~50MB | ~10MB |

## 빠른 시작

### 1. 의존성 추가

```xml
<!-- 코어 API + OpenAI 프로바이더 -->
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

### 2. Hello World (3줄)

```java
import io.llmkit.LlmClient;
import io.llmkit.LlmKit;
import io.llmkit.Providers;

LlmClient client = LlmKit.create("sk-xxx");
String answer = client.chat("양자 컴퓨팅을 한 문장으로 설명해 주세요");
System.out.println(answer);
```

## 사용법

### 프로바이더 전환

```java
// Anthropic
LlmClient anthropic = LlmKit.builder(Providers.ANTHROPIC)
    .apiKey("sk-ant-xxx")
    .model("claude-sonnet-4-20250514")
    .build();
```

### 스트리밍

```java
client.chatStream(
    ChatRequest.builder()
        .addMessage(ChatMessage.user("시를 써 주세요"))
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

### 커스텀 엔드포인트 (DeepSeek, Ollama, Moonshot 등)

```java
LlmClient deepseek = LlmKit.builder(Providers.OPENAI)
    .apiKey("sk-xxx")
    .baseUrl("https://api.deepseek.com/v1")
    .model("deepseek-chat")
    .build();
```

### 전체 설정

```java
LlmClient client = LlmKit.builder(Providers.OPENAI)
    .apiKey("sk-xxx")
    .model("gpt-4o")
    .baseUrl("https://api.openai.com")       // 선택사항
    .timeout(Duration.ofSeconds(60))          // 선택사항
    .retry(RetryPolicy.builder()              // 선택사항
        .maxRetries(3)
        .initialDelayMs(1000)
        .backoffMultiplier(2.0)
        .build())
    .build();
```

### 함수 호출 / 도구 사용

```java
ToolDefinition weatherTool = ToolDefinition.builder()
    .name("get_weather")
    .description("지정된 위치의 현재 날씨 가져오기")
    .parameters("{\"type\":\"object\",\"properties\":{\"location\":{\"type\":\"string\"}},\"required\":[\"location\"]}")
    .build();

ChatResponse response = client.chat(ChatRequest.builder()
    .addMessage(ChatMessage.user("도쿄의 날씨가 어떤가요?"))
    .tools(Collections.singletonList(weatherTool))
    .build());

// 모델이 도구를 호출하려는지 확인
if (response.content() == null && !response.getChoices().get(0).getMessage().getToolCalls().isEmpty()) {
    ToolCall call = response.getChoices().get(0).getMessage().getToolCalls().get(0);
    // 함수를 실행한 후 결과를 다시 전송
}
```

## 아키텍처

```
llmkit-api       코어 API (제로 의존성)
llmkit-core      HTTP, SSE, JSON, 재시도 로직
llmkit-openai    OpenAI 프로바이더 (Chat Completions API)
llmkit-anthropic Anthropic 프로바이더 (Messages API)
llmkit-examples  예제 코드
```

### 설계 원칙

- **제로 의존성**: `llmkit-api`는 서드파티 라이브러리를 전혀 사용하지 않습니다
- **수작성 JSON**: Jackson/Gson 없음 —— OpenAI/Anthropic의 고정 형식만 처리
- **JDK HttpURLConnection**: OkHttp/Apache HttpClient 불필요
- **Java ServiceLoader**: 표준 SPI 메커니즘으로 프로바이더 검색
- **불변 모델**: 모든 모델 클래스는 `final`이며 Builder 패턴 사용, Lombok 없음

## 빌드

```bash
mvn clean compile       # 컴파일
mvn spotless:apply      # 코드 포맷팅
mvn spotless:check      # 포맷 검사 (CI)
```

## 라이선스

[MIT](LICENSE)
