package io.llmkit.anthropic;

import io.llmkit.internal.json.JsonReader;
import io.llmkit.internal.json.JsonWriter;
import io.llmkit.model.ChatChunk;
import io.llmkit.model.ChatChunk.ChunkChoice;
import io.llmkit.model.ChatMessage;
import io.llmkit.model.ChatRequest;
import io.llmkit.model.ChatResponse;
import io.llmkit.model.ChatResponse.Choice;
import io.llmkit.model.ToolCall;
import io.llmkit.model.ToolDefinition;
import io.llmkit.model.Usage;
import java.util.ArrayList;
import java.util.List;

/** Encodes requests and decodes responses for the Anthropic Messages API. */
final class AnthropicCodec {

  private AnthropicCodec() {}

  static String encodeRequest(ChatRequest request, String apiKey) {
    JsonWriter w = new JsonWriter();
    if (request.getModel() != null) {
      w.field("model", request.getModel());
    }
    if (request.getMaxTokens() != null) {
      w.field("max_tokens", request.getMaxTokens());
    } else {
      w.field("max_tokens", 4096);
    }
    if (request.getTemperature() != null) {
      w.field("temperature", request.getTemperature());
    }

    // Anthropic uses a separate system field
    String systemContent = null;
    List<ChatMessage> nonSystem = new ArrayList<>();
    for (ChatMessage msg : request.getMessages()) {
      if (msg.getRole() == ChatMessage.Role.SYSTEM) {
        systemContent = msg.getContent();
      } else {
        nonSystem.add(msg);
      }
    }
    if (systemContent != null) {
      w.field("system", systemContent);
    }

    // Messages array (excluding system)
    JsonWriter.ArrayWriter msgArr = w.array("messages");
    for (ChatMessage msg : nonSystem) {
      msgArr.raw(encodeMessage(msg));
    }
    msgArr.end();

    // Tools array
    if (!request.getTools().isEmpty()) {
      JsonWriter.ArrayWriter toolsArr = w.array("tools");
      for (ToolDefinition tool : request.getTools()) {
        toolsArr.raw(encodeToolDefinition(tool));
      }
      toolsArr.end();
    }

    return w.build();
  }

  static String encodeMessage(ChatMessage msg) {
    JsonWriter w = new JsonWriter();
    // Anthropic uses "user"/"assistant" (lowercase) — tool results use "user" role
    if (msg.getRole() == ChatMessage.Role.TOOL) {
      // Anthropic tool result format
      w.field("role", "user");
      // content is an array of tool_result blocks
      JsonWriter.ArrayWriter contentArr = w.array("content");
      JsonWriter toolResultObj = new JsonWriter();
      toolResultObj.field("type", "tool_result");
      toolResultObj.field("tool_use_id", msg.getToolCallId());
      toolResultObj.field("content", msg.getContent());
      contentArr.raw(toolResultObj.build());
      contentArr.end();
    } else {
      w.field("role", msg.getRole().name().toLowerCase());
      if (msg.getContent() != null) {
        w.field("content", msg.getContent());
      }
      if (!msg.getToolCalls().isEmpty()) {
        JsonWriter.ArrayWriter contentArr = w.array("content");
        if (msg.getContent() != null) {
          JsonWriter textBlock = new JsonWriter();
          textBlock.field("type", "text");
          textBlock.field("text", msg.getContent());
          contentArr.raw(textBlock.build());
        }
        for (ToolCall tc : msg.getToolCalls()) {
          JsonWriter toolBlock = new JsonWriter();
          toolBlock.field("type", "tool_use");
          toolBlock.field("id", tc.getId());
          toolBlock.field("name", tc.getName());
          toolBlock.rawFieldDirect("input", tc.getArguments() != null ? tc.getArguments() : "{}");
          contentArr.raw(toolBlock.build());
        }
        contentArr.end();
      }
    }
    return w.build();
  }

  private static String encodeToolDefinition(ToolDefinition tool) {
    JsonWriter w = new JsonWriter();
    w.field("name", tool.getName());
    if (tool.getDescription() != null) {
      w.field("description", tool.getDescription());
    }
    if (tool.getParameters() != null) {
      w.rawFieldDirect("input_schema", tool.getParameters());
    }
    return w.build();
  }

  static ChatResponse decodeResponse(String json) {
    JsonReader r = new JsonReader(json);
    String id = r.readString("id");
    String model = r.readString("model");

    Usage usage = null;
    int usageIdx = json.indexOf("\"usage\"");
    if (usageIdx >= 0) {
      usage = decodeUsage(json, usageIdx);
    }

    List<Choice> choices = new ArrayList<>();
    // Anthropic has "content" array instead of "choices"
    String content = extractContentText(json);
    List<ToolCall> toolCalls = extractToolCalls(json);

    ChatMessage.Builder mb = ChatMessage.builder().role(ChatMessage.Role.ASSISTANT);
    if (content != null) {
      mb.content(content);
    }
    if (!toolCalls.isEmpty()) {
      mb.toolCalls(toolCalls);
    }
    choices.add(new Choice(0, mb.build(), extractStopReason(json)));

    return new ChatResponse(id, choices, usage, model);
  }

  static ChatChunk decodeChunk(String json) {
    JsonReader r = new JsonReader(json);
    String type = r.readString("type");
    String id = r.readString("message") != null ? r.readString("message") : "";

    List<ChunkChoice> choices = new ArrayList<>();

    if ("content_block_delta".equals(type)) {
      String deltaText = r.readString("text");
      if (deltaText == null) {
        // Try to extract from delta object
        deltaText = extractDeltaText(json);
      }
      ChatMessage delta =
          ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).content(deltaText).build();
      choices.add(new ChunkChoice(0, delta, null));
    } else if ("message_delta".equals(type)) {
      String stopReason = extractStopReasonDelta(json);
      ChatMessage delta = ChatMessage.builder().role(ChatMessage.Role.ASSISTANT).build();
      choices.add(new ChunkChoice(0, delta, stopReason));
    } else if ("message_start".equals(type)
        || "message_stop".equals(type)
        || "content_block_start".equals(type)
        || "content_block_stop".equals(type)
        || "ping".equals(type)) {
      // These event types don't carry content deltas, return empty chunk
      return null;
    }

    return new ChatChunk(id, choices);
  }

  private static String extractContentText(String json) {
    int contentIdx = json.indexOf("\"content\"");
    if (contentIdx < 0) return null;

    int arrStart = json.indexOf('[', contentIdx);
    if (arrStart < 0) return null;

    StringBuilder sb = new StringBuilder();
    int pos = arrStart + 1;
    while (pos < json.length()) {
      int objStart = json.indexOf("{", pos);
      if (objStart < 0) break;
      if (objStart > json.indexOf(']', pos)) break;

      int[] bounds = new JsonReader(json).findObjectBounds(objStart);
      if (bounds == null) break;

      JsonReader cr = new JsonReader(json.substring(bounds[0], bounds[1]));
      String blockType = cr.readString("type");
      if ("text".equals(blockType)) {
        String text = cr.readString("text");
        if (text != null) {
          sb.append(text);
        }
      }
      pos = bounds[1];
    }
    return sb.length() > 0 ? sb.toString() : null;
  }

  private static List<ToolCall> extractToolCalls(String json) {
    List<ToolCall> toolCalls = new ArrayList<>();
    int contentIdx = json.indexOf("\"content\"");
    if (contentIdx < 0) return toolCalls;

    int arrStart = json.indexOf('[', contentIdx);
    if (arrStart < 0) return toolCalls;

    int pos = arrStart + 1;
    while (pos < json.length()) {
      int objStart = json.indexOf("{", pos);
      if (objStart < 0) break;
      if (objStart > json.indexOf(']', pos)) break;

      int[] bounds = new JsonReader(json).findObjectBounds(objStart);
      if (bounds == null) break;

      JsonReader cr = new JsonReader(json.substring(bounds[0], bounds[1]));
      String blockType = cr.readString("type");
      if ("tool_use".equals(blockType)) {
        String id = cr.readString("id");
        String name = cr.readString("name");
        String input = cr.readString("input");
        toolCalls.add(new ToolCall(id, name, input));
      }
      pos = bounds[1];
    }
    return toolCalls;
  }

  private static String extractStopReason(String json) {
    int idx = json.indexOf("\"stop_reason\"");
    if (idx < 0) return null;
    JsonReader r = new JsonReader(json);
    r.setPosition(idx);
    return r.readString("stop_reason");
  }

  private static String extractDeltaText(String json) {
    int deltaIdx = json.indexOf("\"delta\"");
    if (deltaIdx < 0) return null;

    int objStart = json.indexOf('{', deltaIdx);
    if (objStart < 0) return null;

    int[] bounds = new JsonReader(json).findObjectBounds(objStart);
    if (bounds == null) return null;

    JsonReader dr = new JsonReader(json.substring(bounds[0], bounds[1]));
    return dr.readString("text");
  }

  private static String extractStopReasonDelta(String json) {
    int deltaIdx = json.indexOf("\"delta\"");
    if (deltaIdx < 0) return null;

    int objStart = json.indexOf('{', deltaIdx);
    if (objStart < 0) return null;

    int[] bounds = new JsonReader(json).findObjectBounds(objStart);
    if (bounds == null) return null;

    JsonReader dr = new JsonReader(json.substring(bounds[0], bounds[1]));
    return dr.readString("stop_reason");
  }

  private static Usage decodeUsage(String json, int usageIdx) {
    int colonIdx = json.indexOf(':', usageIdx + 6);
    if (colonIdx < 0) return null;

    JsonReader r = new JsonReader(json);
    int[] bounds = r.findObjectBounds(colonIdx + 1);
    if (bounds == null) return null;

    JsonReader sub = r.subReader(bounds[0], bounds[1]);
    Integer input = sub.readInt("input_tokens");
    Integer output = sub.readInt("output_tokens");

    return new Usage(
        input != null ? input : 0,
        output != null ? output : 0,
        (input != null ? input : 0) + (output != null ? output : 0));
  }
}
