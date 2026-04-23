package io.llmkit.anthropic;

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

class AnthropicCodecTest {

  // ========== Encoding Tests ==========

  @Test
  void encodeBasicRequest() {
    ChatRequest req =
        ChatRequest.builder()
            .model("claude-sonnet-4-20250514")
            .addMessage(ChatMessage.user("hello"))
            .maxTokens(50)
            .temperature(0.5)
            .build();
    String json = AnthropicCodec.encodeRequest(req, "sk-ant-test");
    assertTrue(json.contains("\"model\":\"claude-sonnet-4-20250514\""));
    assertTrue(json.contains("\"max_tokens\":50"));
    assertTrue(json.contains("\"temperature\":0.5"));
    assertTrue(json.contains("\"role\":\"user\""));
    assertTrue(json.contains("\"content\":\"hello\""));
  }

  @Test
  void encodeSystemFieldExtracted() {
    ChatRequest req =
        ChatRequest.builder()
            .model("claude-sonnet-4-20250514")
            .addMessage(ChatMessage.system("you are helpful"))
            .addMessage(ChatMessage.user("hi"))
            .build();
    String json = AnthropicCodec.encodeRequest(req, "key");
    assertTrue(json.contains("\"system\":\"you are helpful\""));
    // system message should NOT be in messages array
    int sysIdx = json.indexOf("\"system\"");
    int msgIdx = json.indexOf("\"messages\"");
    assertTrue(sysIdx > 0);
    assertTrue(msgIdx > 0);
    // messages should contain user but not system
    String msgSection = json.substring(msgIdx);
    assertFalse(msgSection.contains("\"role\":\"system\""));
    assertTrue(msgSection.contains("\"role\":\"user\""));
  }

  @Test
  void encodeDefaultMaxTokens() {
    ChatRequest req = ChatRequest.builder().model("m").addMessage(ChatMessage.user("hi")).build();
    String json = AnthropicCodec.encodeRequest(req, "key");
    assertTrue(json.contains("\"max_tokens\":4096"));
  }

  @Test
  void encodeToolResultMessage() {
    ChatMessage msg = ChatMessage.toolResult("toolu-123", "sunny, 22C");
    String json = AnthropicCodec.encodeMessage(msg);
    assertTrue(json.contains("\"role\":\"user\""));
    assertTrue(json.contains("\"type\":\"tool_result\""));
    assertTrue(json.contains("\"tool_use_id\":\"toolu-123\""));
    assertTrue(json.contains("\"content\":\"sunny, 22C\""));
  }

  @Test
  void encodeAssistantWithToolCalls() {
    ChatMessage msg =
        ChatMessage.builder()
            .role(ChatMessage.Role.ASSISTANT)
            .content("checking")
            .toolCalls(Arrays.asList(new ToolCall("toolu-1", "weather", "{\"loc\":\"TK\"}")))
            .build();
    String json = AnthropicCodec.encodeMessage(msg);
    assertTrue(json.contains("\"role\":\"assistant\""));
    assertTrue(json.contains("\"type\":\"text\""));
    assertTrue(json.contains("\"type\":\"tool_use\""));
    assertTrue(json.contains("\"id\":\"toolu-1\""));
    assertTrue(json.contains("\"name\":\"weather\""));
    assertTrue(json.contains("\"input\""));
  }

  @Test
  void encodeToolsDefinition() {
    ToolDefinition tool =
        ToolDefinition.builder()
            .name("calc")
            .description("Calculate")
            .parameters("{\"type\":\"object\"}")
            .build();
    ChatRequest req =
        ChatRequest.builder()
            .model("m")
            .addMessage(ChatMessage.user("calc"))
            .tools(Collections.singletonList(tool))
            .build();
    String json = AnthropicCodec.encodeRequest(req, "key");
    assertTrue(json.contains("\"tools\""));
    assertTrue(json.contains("\"name\":\"calc\""));
    assertTrue(json.contains("\"input_schema\""));
  }

  // ========== Response Decoding Tests ==========

  @Test
  void decodeBasicResponse() {
    String json =
        "{\"id\":\"msg_123\",\"type\":\"message\",\"role\":\"assistant\",\"model\":\"claude-sonnet-4-20250514\","
            + "\"content\":[{\"type\":\"text\",\"text\":\"Hello from Claude!\"}],"
            + "\"stop_reason\":\"end_turn\","
            + "\"usage\":{\"input_tokens\":10,\"output_tokens\":8}}";

    ChatResponse resp = AnthropicCodec.decodeResponse(json);
    assertEquals("msg_123", resp.getId());
    assertEquals("claude-sonnet-4-20250514", resp.getModel());
    assertEquals("Hello from Claude!", resp.content());
    assertEquals("end_turn", resp.getChoices().get(0).getFinishReason());
    assertEquals(10, resp.getUsage().getPromptTokens());
    assertEquals(8, resp.getUsage().getCompletionTokens());
    assertEquals(18, resp.getUsage().getTotalTokens());
  }

  @Test
  void decodeResponseWithToolUse() {
    String json =
        "{\"id\":\"msg_456\",\"type\":\"message\",\"role\":\"assistant\",\"model\":\"claude-sonnet-4-20250514\","
            + "\"content\":[{\"type\":\"text\",\"text\":\"Let me check.\"},"
            + "{\"type\":\"tool_use\",\"id\":\"toolu_abc\",\"name\":\"get_weather\",\"input\":{\"location\":\"Tokyo\"}}],"
            + "\"stop_reason\":\"tool_use\","
            + "\"usage\":{\"input_tokens\":15,\"output_tokens\":20}}";

    ChatResponse resp = AnthropicCodec.decodeResponse(json);
    assertEquals("Let me check.", resp.content());
    ChatMessage msg = resp.getChoices().get(0).getMessage();
    assertEquals(1, msg.getToolCalls().size());
    assertEquals("toolu_abc", msg.getToolCalls().get(0).getId());
    assertEquals("get_weather", msg.getToolCalls().get(0).getName());
    assertEquals("tool_use", resp.getChoices().get(0).getFinishReason());
  }

  @Test
  void decodeResponseWithoutUsage() {
    String json =
        "{\"id\":\"x\",\"content\":[{\"type\":\"text\",\"text\":\"hi\"}],\"stop_reason\":\"end_turn\"}";

    ChatResponse resp = AnthropicCodec.decodeResponse(json);
    assertNull(resp.getUsage());
    assertEquals("hi", resp.content());
  }

  // ========== Chunk Decoding Tests ==========

  @Test
  void decodeContentBlockDelta() {
    String json =
        "{\"type\":\"content_block_delta\",\"index\":0,\"delta\":{\"type\":\"text_delta\",\"text\":\"Hello\"}}";

    ChatChunk chunk = AnthropicCodec.decodeChunk(json);
    assertNotNull(chunk);
    assertEquals("Hello", chunk.delta());
  }

  @Test
  void decodeMessageDeltaWithStopReason() {
    String json =
        "{\"type\":\"message_delta\",\"delta\":{\"stop_reason\":\"end_turn\"},\"usage\":{\"output_tokens\":5}}";

    ChatChunk chunk = AnthropicCodec.decodeChunk(json);
    assertNotNull(chunk);
    assertEquals("end_turn", chunk.getChoices().get(0).getFinishReason());
  }

  @Test
  void decodeMessageStartReturnsNull() {
    String json = "{\"type\":\"message_start\",\"message\":{\"id\":\"msg_1\"}}";
    assertNull(AnthropicCodec.decodeChunk(json));
  }

  @Test
  void decodeMessageStopReturnsNull() {
    String json = "{\"type\":\"message_stop\"}";
    assertNull(AnthropicCodec.decodeChunk(json));
  }

  @Test
  void decodePingReturnsNull() {
    String json = "{\"type\":\"ping\"}";
    assertNull(AnthropicCodec.decodeChunk(json));
  }

  @Test
  void decodeContentBlockStartReturnsNull() {
    String json = "{\"type\":\"content_block_start\",\"index\":0}";
    assertNull(AnthropicCodec.decodeChunk(json));
  }

  @Test
  void decodeContentBlockStopReturnsNull() {
    String json = "{\"type\":\"content_block_stop\",\"index\":0}";
    assertNull(AnthropicCodec.decodeChunk(json));
  }
}
