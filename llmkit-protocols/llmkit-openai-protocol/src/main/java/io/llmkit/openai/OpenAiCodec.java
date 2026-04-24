package io.llmkit.openai;

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

/** Encodes requests and decodes responses for the OpenAI Chat Completions API. */
public final class OpenAiCodec {

  private OpenAiCodec() {}

  public static String encodeRequest(ChatRequest request) {
    JsonWriter w = new JsonWriter();
    if (request.getModel() != null) {
      w.field("model", request.getModel());
    }
    if (request.getTemperature() != null) {
      w.field("temperature", request.getTemperature());
    }
    if (request.getMaxTokens() != null) {
      w.field("max_tokens", request.getMaxTokens());
    }

    // Messages array
    JsonWriter.ArrayWriter msgArr = w.array("messages");
    for (ChatMessage msg : request.getMessages()) {
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

  public static String encodeMessage(ChatMessage msg) {
    JsonWriter w = new JsonWriter();
    w.field("role", msg.getRole().name().toLowerCase());
    if (msg.getContent() != null) {
      w.field("content", msg.getContent());
    }
    if (msg.getToolCallId() != null) {
      w.field("tool_call_id", msg.getToolCallId());
    }
    if (!msg.getToolCalls().isEmpty()) {
      JsonWriter.ArrayWriter tcArr = w.array("tool_calls");
      for (ToolCall tc : msg.getToolCalls()) {
        tcArr.raw(encodeToolCall(tc));
      }
      tcArr.end();
    }
    return w.build();
  }

  private static String encodeToolCall(ToolCall tc) {
    JsonWriter w = new JsonWriter();
    w.field("id", tc.getId());
    w.field("type", "function");
    // function object
    JsonWriter funcObj = new JsonWriter();
    funcObj.field("name", tc.getName());
    funcObj.rawField("arguments", tc.getArguments() != null ? tc.getArguments() : "{}");
    w.rawField("function", funcObj.build());
    return w.build();
  }

  private static String encodeToolDefinition(ToolDefinition tool) {
    JsonWriter w = new JsonWriter();
    w.field("type", "function");
    JsonWriter funcObj = new JsonWriter();
    funcObj.field("name", tool.getName());
    if (tool.getDescription() != null) {
      funcObj.field("description", tool.getDescription());
    }
    if (tool.getParameters() != null) {
      funcObj.rawFieldDirect("parameters", tool.getParameters());
    }
    w.rawField("function", funcObj.build());
    return w.build();
  }

  public static ChatResponse decodeResponse(String json) {
    JsonReader r = new JsonReader(json);
    String id = r.readString("id");
    String model = r.readString("model");

    Usage usage = null;
    int usageIdx = json.indexOf("\"usage\"");
    if (usageIdx >= 0) {
      usage = decodeUsage(json, usageIdx);
    }

    List<Choice> choices = new ArrayList<>();
    int arrPos = r.findArray("choices");
    if (arrPos >= 0) {
      r.setPosition(arrPos);
      decodeChoices(r, choices);
    }

    return new ChatResponse(id, choices, usage, model);
  }

  public static ChatChunk decodeChunk(String json) {
    JsonReader r = new JsonReader(json);
    String id = r.readString("id");

    List<ChunkChoice> choices = new ArrayList<>();
    int arrPos = r.findArray("choices");
    if (arrPos >= 0) {
      r.setPosition(arrPos);
      decodeChunkChoices(r, choices);
    }

    return new ChatChunk(id, choices);
  }

  private static void decodeChoices(JsonReader r, List<Choice> choices) {
    String json = r.getJson();
    int pos = r.getPosition();
    while (pos < json.length()) {
      // Find next object
      while (pos < json.length() && json.charAt(pos) != '{' && json.charAt(pos) != ']') {
        pos++;
      }
      if (pos >= json.length() || json.charAt(pos) == ']') break;

      int[] bounds = r.findObjectBounds(pos);
      if (bounds == null) break;

      JsonReader cr = r.subReader(bounds[0], bounds[1]);
      int index = cr.readInt("index") != null ? cr.readInt("index") : 0;
      String finishReason = cr.readString("finish_reason");

      ChatMessage message = null;
      int msgIdx = json.indexOf("\"message\"", bounds[0]);
      if (msgIdx >= 0 && msgIdx < bounds[1]) {
        message = decodeMessageFromJson(json, msgIdx);
      }

      choices.add(new Choice(index, message, finishReason));
      pos = bounds[1];
    }
  }

  private static void decodeChunkChoices(JsonReader r, List<ChunkChoice> choices) {
    String json = r.getJson();
    int pos = r.getPosition();
    while (pos < json.length()) {
      while (pos < json.length() && json.charAt(pos) != '{' && json.charAt(pos) != ']') {
        pos++;
      }
      if (pos >= json.length() || json.charAt(pos) == ']') break;

      int[] bounds = r.findObjectBounds(pos);
      if (bounds == null) break;

      JsonReader cr = r.subReader(bounds[0], bounds[1]);
      int index = cr.readInt("index") != null ? cr.readInt("index") : 0;
      String finishReason = cr.readString("finish_reason");

      ChatMessage delta = null;
      int deltaIdx = json.indexOf("\"delta\"", bounds[0]);
      if (deltaIdx >= 0 && deltaIdx < bounds[1]) {
        delta = decodeDeltaFromJson(json, deltaIdx, bounds[1]);
      }

      choices.add(new ChunkChoice(index, delta, finishReason));
      pos = bounds[1];
    }
  }

  private static ChatMessage decodeMessageFromJson(String json, int msgKeyIdx) {
    int colonIdx = json.indexOf(':', msgKeyIdx + 9);
    if (colonIdx < 0) return null;

    int objStart = colonIdx + 1;
    JsonReader mr = new JsonReader(json);
    int[] bounds = mr.findObjectBounds(objStart);
    if (bounds == null) return null;

    JsonReader sub = mr.subReader(bounds[0], bounds[1]);
    String roleStr = sub.readString("role");
    String content = sub.readString("content");

    ChatMessage.Role role = null;
    if (roleStr != null) {
      role = ChatMessage.Role.valueOf(roleStr.toUpperCase());
    }

    ChatMessage.Builder mb = ChatMessage.builder();
    if (role != null) mb.role(role);
    if (content != null) mb.content(content);

    // Decode tool_calls
    List<ToolCall> toolCalls = new ArrayList<>();
    int tcArrIdx = json.indexOf("\"tool_calls\"", bounds[0]);
    if (tcArrIdx >= 0 && tcArrIdx < bounds[1]) {
      int tcArrStart = json.indexOf('[', tcArrIdx);
      if (tcArrStart >= 0) {
        int tcPos = tcArrStart + 1;
        while (tcPos < json.length()) {
          while (tcPos < json.length() && json.charAt(tcPos) != '{' && json.charAt(tcPos) != ']') {
            tcPos++;
          }
          if (tcPos >= json.length() || json.charAt(tcPos) == ']') break;

          int[] tcBounds = new JsonReader(json).findObjectBounds(tcPos);
          if (tcBounds == null) break;

          JsonReader tcr = new JsonReader(json.substring(tcBounds[0], tcBounds[1]));
          String tcId = tcr.readString("id");
          String tcType = tcr.readString("type");
          String funcName = tcr.readString("name");
          String funcArgs = tcr.readString("arguments");

          toolCalls.add(new ToolCall(tcId, funcName, funcArgs));
          tcPos = tcBounds[1];
        }
      }
    }
    if (!toolCalls.isEmpty()) {
      mb.toolCalls(toolCalls);
    }

    return mb.build();
  }

  private static ChatMessage decodeDeltaFromJson(String json, int deltaKeyIdx, int parentEnd) {
    int colonIdx = json.indexOf(':', deltaKeyIdx + 7);
    if (colonIdx < 0) return null;

    int objStart = colonIdx + 1;
    JsonReader dr = new JsonReader(json);
    int[] bounds = dr.findObjectBounds(objStart);
    if (bounds == null) return null;

    JsonReader sub = dr.subReader(bounds[0], bounds[1]);
    String roleStr = sub.readString("role");
    String content = sub.readString("content");

    ChatMessage.Builder mb = ChatMessage.builder();
    if (roleStr != null) {
      mb.role(ChatMessage.Role.valueOf(roleStr.toUpperCase()));
    } else {
      mb.role(ChatMessage.Role.ASSISTANT);
    }
    if (content != null) {
      mb.content(content);
    }
    return mb.build();
  }

  private static Usage decodeUsage(String json, int usageIdx) {
    JsonReader r = new JsonReader(json);
    r.setPosition(usageIdx);
    int colonIdx = json.indexOf(':', usageIdx + 6);
    if (colonIdx < 0) return null;

    int[] bounds = r.findObjectBounds(colonIdx + 1);
    if (bounds == null) return null;

    JsonReader sub = r.subReader(bounds[0], bounds[1]);
    Integer prompt = sub.readInt("prompt_tokens");
    Integer completion = sub.readInt("completion_tokens");
    Integer total = sub.readInt("total_tokens");

    return new Usage(
        prompt != null ? prompt : 0,
        completion != null ? completion : 0,
        total != null ? total : 0);
  }
}
