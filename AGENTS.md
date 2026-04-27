# AGENTS.md

This file provides guidance to ai agents when working with code in this repository.

## Project Overview

llmkit is a lightweight, zero-dependency, multi-provider Java LLM client library targeting JDK 8+. It provides a unified API for calling chat completion endpoints from multiple LLM providers (OpenAI, Anthropic, DeepSeek, GLM, Qwen, MiniMax, Kimi). No Spring, Lombok, Jackson, or any external dependencies.

## Build Commands

```bash
mvn clean compile              # Compile all modules
mvn test                        # Run unit tests (excludes integration tests)
mvn test -pl llmkit-core        # Run tests in a single module
mvn test -Dtest=JsonReaderTest  # Run a single test class
mvn test -P it                  # Run integration tests only
mvn spotless:apply              # Auto-format code (Google Java Format, requires JDK 11+)
mvn spotless:check              # Check formatting (CI enforced)
```

## Architecture

Multi-module Maven project with a layered architecture:

```
llmkit-api          → Public API surface (LlmClient, LlmKit, LlmProvider, model classes). Zero dependencies.
llmkit-core         → Internal infrastructure (AbstractLlmClient template, HTTP client, JSON parser/writer, SSE parser).
llmkit-protocols/   → Protocol implementations:
  llmkit-openai-protocol    → OpenAI API codec (also used by 5 OpenAI-compatible providers)
  llmkit-anthropic-protocol → Anthropic API codec
llmkit-providers/   → Provider modules (one per LLM vendor, each is 3 classes):
  llmkit-openai, llmkit-anthropic, llmkit-deepseek, llmkit-glm, llmkit-qwen, llmkit-minimax, llmkit-kimi
llmkit-examples     → Usage examples depending on all providers
```

**Key patterns:**
- **Template Method**: `AbstractLlmClient` in llmkit-core defines the algorithm skeleton (retry, HTTP, streaming). Provider subclasses implement `defaultBaseUrl()`, `endpointPath()`, `buildHeaders()`, `encodeRequest()`, `decodeResponse()`, `decodeChunk()`.
- **Java SPI (ServiceLoader)**: Each provider registers via `META-INF/services/io.llmkit.LlmProvider`. `LlmKit.loadProviders()` discovers them at runtime.
- **Protocol reuse**: 6 providers (OpenAI, DeepSeek, GLM, Qwen, MiniMax, Kimi) share `llmkit-openai-protocol`. Only Anthropic has its own protocol module.
- **Hand-written JSON**: `JsonReader`/`JsonWriter` in llmkit-core handle only the fixed patterns needed for API formats — no external JSON library.
- **Immutable models**: All model classes use Builder pattern with `final` fields, private constructors, and unmodifiable collections.

## Adding a New Provider

When adding a new OpenAI-compatible provider:
1. Create module `llmkit-providers/llmkit-xxx` depending on `llmkit-openai-protocol`
2. Implement 3 classes: `XxxProvider` (SPI), `XxxClient` (extends `AbstractOpenAiClient`), `XxxClientBuilder` (extends `AbstractOpenAiClientBuilder`)
3. Register in `META-INF/services/io.llmkit.LlmProvider`
4. Add module to root `pom.xml` `<modules>` and `<dependencyManagement>`
5. Add constant to `Providers` in llmkit-api

## Code Style

- Google Java Format (enforced via Spotless, JDK 11+)
- JDK 8 source/target compatibility — no lambda method references on generic types, no `var`, no `List.of()`
- No Lombok, no external annotations
