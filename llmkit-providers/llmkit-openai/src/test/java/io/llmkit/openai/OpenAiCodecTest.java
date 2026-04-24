package io.llmkit.openai;

import static org.junit.jupiter.api.Assertions.*;

import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatMessage;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import io.llmkit.model.ToolCall;
import io.llmkit.model.ToolDefinition;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class OpenAiCodecTest {

  // ========== Encoding Tests ==========

  @Test
  void encodeBasicRequest() {
    ChatRequest req =
        ChatRequest.builder()
            .model("gpt-4o")
            .addMessage(ChatMessage.user("hello"))
            .temperature(0.7)
            .maxTokens(100)
            .build();
    String json = OpenAiCodec.encodeRequest(req);
    assertTrue(json.contains("\"model\":\"gpt-4o\""));
    assertTrue(json.contains("\"temperature\":0.7"));
    assertTrue(json.contains("\"max_tokens\":100"));
    assertTrue(json.contains("\"messages\""));
    assertTrue(json.contains("\"role\":\"user\""));
    assertTrue(json.contains("\"content\":\"hello\""));
  }

  @Test
  void encodeSystemMessage() {
    ChatRequest req =
        ChatRequest.builder()
            .model("gpt-4o")
            .addMessage(ChatMessage.system("you are helpful"))
            .addMessage(ChatMessage.user("hi"))
            .build();
    String json = OpenAiCodec.encodeRequest(req);
    assertTrue(json.contains("\"role\":\"system\""));
    assertTrue(json.contains("\"role\":\"user\""));
  }

  @Test
  void encodeToolResultMessage() {
    ChatRequest req =
        ChatRequest.builder()
            .model("gpt-4o")
            .addMessage(ChatMessage.toolResult("call-123", "sunny"))
            .build();
    String json = OpenAiCodec.encodeRequest(req);
    assertTrue(json.contains("\"role\":\"tool\""));
    assertTrue(json.contains("\"tool_call_id\":\"call-123\""));
    assertTrue(json.contains("\"content\":\"sunny\""));
  }

  @Test
  void encodeRequestWithTools() {
    ToolDefinition tool =
        ToolDefinition.builder()
            .name("get_weather")
            .description("Get weather")
            .parameters("{\"type\":\"object\"}")
            .build();
    ChatRequest req =
        ChatRequest.builder()
            .model("gpt-4o")
            .addMessage(ChatMessage.user("weather?"))
            .tools(Collections.singletonList(tool))
            .build();
    String json = OpenAiCodec.encodeRequest(req);
    assertTrue(json.contains("\"tools\""));
    assertTrue(json.contains("\"type\":\"function\""));
    assertTrue(json.contains("\"name\":\"get_weather\""));
    assertTrue(json.contains("\"description\":\"Get weather\""));
  }

  @Test
  void encodeMessageWithToolCalls() {
    ChatMessage msg =
        ChatMessage.builder()
            .role(ChatMessage.Role.ASSISTANT)
            .content("let me check")
            .toolCalls(Arrays.asList(new ToolCall("call-1", "get_weather", "{\"loc\":\"TK\"}")))
            .build();
    String json = OpenAiCodec.encodeMessage(msg);
    assertTrue(json.contains("\"tool_calls\""));
    assertTrue(json.contains("\"id\":\"call-1\""));
    assertTrue(json.contains("\"name\":\"get_weather\""));
    assertTrue(json.contains("\"arguments\""));
    assertTrue(json.contains("\"type\":\"function\""));
  }

  @Test
  void encodeNullFieldsOmitted() {
    ChatRequest req =
        ChatRequest.builder().model("gpt-4o").addMessage(ChatMessage.user("hi")).build();
    String json = OpenAiCodec.encodeRequest(req);
    assertFalse(json.contains("temperature"));
    assertFalse(json.contains("max_tokens"));
    assertFalse(json.contains("tools"));
  }

  // ========== Response Decoding Tests ==========

  @Test
  void decodeBasicResponse() {
    String json =
        "{\"id\":\"chatcmpl-123\",\"object\":\"chat.completion\",\"model\":\"gpt-4o\","
            + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"Hello!\"},\"finish_reason\":\"stop\"}],"
            + "\"usage\":{\"prompt_tokens\":10,\"completion_tokens\":5,\"total_tokens\":15}}";

    ChatResponse resp = OpenAiCodec.decodeResponse(json);
    assertEquals("chatcmpl-123", resp.getId());
    assertEquals("gpt-4o", resp.getModel());
    assertEquals("Hello!", resp.content());
    assertEquals("stop", resp.getChoices().get(0).getFinishReason());
    assertEquals(10, resp.getUsage().getPromptTokens());
    assertEquals(5, resp.getUsage().getCompletionTokens());
    assertEquals(15, resp.getUsage().getTotalTokens());
  }

  @Test
  void decodeResponseWithToolCalls() {
    String json =
        "{\"id\":\"chatcmpl-456\",\"model\":\"gpt-4o\","
            + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":null,"
            + "\"tool_calls\":[{\"id\":\"call-abc\",\"type\":\"function\",\"function\":{\"name\":\"get_weather\",\"arguments\":\"{\\\"city\\\":\\\"Tokyo\\\"}\"}}]},"
            + "\"finish_reason\":\"tool_calls\"}],"
            + "\"usage\":{\"prompt_tokens\":20,\"completion_tokens\":10,\"total_tokens\":30}}";

    ChatResponse resp = OpenAiCodec.decodeResponse(json);
    assertNull(resp.content());
    ChatMessage msg = resp.getChoices().get(0).getMessage();
    assertEquals(1, msg.getToolCalls().size());
    assertEquals("call-abc", msg.getToolCalls().get(0).getId());
    assertEquals("get_weather", msg.getToolCalls().get(0).getName());
    assertEquals("{\"city\":\"Tokyo\"}", msg.getToolCalls().get(0).getArguments());
    assertEquals("tool_calls", resp.getChoices().get(0).getFinishReason());
  }

  @Test
  void decodeMultipleChoices() {
    String json =
        "{\"id\":\"x\",\"model\":\"m\","
            + "\"choices\":["
            + "{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"A\"},\"finish_reason\":\"stop\"},"
            + "{\"index\":1,\"message\":{\"role\":\"assistant\",\"content\":\"B\"},\"finish_reason\":\"stop\"}"
            + "]}";

    ChatResponse resp = OpenAiCodec.decodeResponse(json);
    assertEquals(2, resp.getChoices().size());
    assertEquals("A", resp.getChoices().get(0).getMessage().getContent());
    assertEquals("B", resp.getChoices().get(1).getMessage().getContent());
  }

  @Test
  void decodeResponseWithoutUsage() {
    String json =
        "{\"id\":\"x\",\"model\":\"m\","
            + "\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"hi\"},\"finish_reason\":\"stop\"}]}";

    ChatResponse resp = OpenAiCodec.decodeResponse(json);
    assertNull(resp.getUsage());
    assertEquals("hi", resp.content());
  }

  // ========== Chunk Decoding Tests ==========

  @Test
  void decodeBasicChunk() {
    String json =
        "{\"id\":\"chatcmpl-789\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"Hello\"},\"finish_reason\":null}]}";

    ChatChunk chunk = OpenAiCodec.decodeChunk(json);
    assertEquals("chatcmpl-789", chunk.getId());
    assertEquals("Hello", chunk.delta());
    assertNull(chunk.getChoices().get(0).getFinishReason());
  }

  @Test
  void decodeChunkWithFinishReason() {
    String json =
        "{\"id\":\"x\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\"},\"finish_reason\":\"stop\"}]}";

    ChatChunk chunk = OpenAiCodec.decodeChunk(json);
    assertEquals("stop", chunk.getChoices().get(0).getFinishReason());
    assertNull(chunk.delta());
  }

  @Test
  void decodeEmptyDeltaChunk() {
    String json =
        "{\"id\":\"x\",\"choices\":[{\"index\":0,\"delta\":{\"role\":\"assistant\",\"content\":\"\"},\"finish_reason\":null}]}";

    ChatChunk chunk = OpenAiCodec.decodeChunk(json);
    assertEquals("", chunk.delta());
  }
}
