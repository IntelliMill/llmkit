package io.llmkit.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class ChatRequestTest {

  @Test
  void builderBasicFields() {
    ChatRequest req =
        ChatRequest.builder()
            .model("gpt-4o")
            .addMessage(ChatMessage.user("hello"))
            .temperature(0.7)
            .maxTokens(100)
            .build();

    assertEquals("gpt-4o", req.getModel());
    assertEquals(1, req.getMessages().size());
    assertEquals(0.7, req.getTemperature());
    assertEquals(100, req.getMaxTokens());
    assertTrue(req.getTools().isEmpty());
  }

  @Test
  void messagesListIsImmutable() {
    ChatRequest req =
        ChatRequest.builder()
            .addMessage(ChatMessage.user("a"))
            .addMessage(ChatMessage.user("b"))
            .build();
    assertEquals(2, req.getMessages().size());
    assertThrows(
        UnsupportedOperationException.class, () -> req.getMessages().add(ChatMessage.user("c")));
  }

  @Test
  void messagesSetterReplaces() {
    ChatRequest req =
        ChatRequest.builder()
            .addMessage(ChatMessage.user("old"))
            .messages(Collections.singletonList(ChatMessage.user("new")))
            .build();
    assertEquals(1, req.getMessages().size());
    assertEquals("new", req.getMessages().get(0).getContent());
  }

  @Test
  void toolsAreImmutable() {
    ToolDefinition td = ToolDefinition.builder().name("f").build();
    ChatRequest req = ChatRequest.builder().tools(Arrays.asList(td)).build();
    assertThrows(UnsupportedOperationException.class, () -> req.getTools().add(null));
  }

  @Test
  void nullFieldsDefault() {
    ChatRequest req = ChatRequest.builder().build();
    assertNull(req.getModel());
    assertNull(req.getTemperature());
    assertNull(req.getMaxTokens());
    assertTrue(req.getMessages().isEmpty());
    assertTrue(req.getTools().isEmpty());
    assertNull(req.getTopP());
    assertNull(req.getSeed());
    assertTrue(req.getStop().isEmpty());
    assertNull(req.getLogprobs());
    assertNull(req.getTopLogprobs());
    assertNull(req.getResponseFormat());
  }

  @Test
  void topPField() {
    ChatRequest req = ChatRequest.builder().topP(0.95).build();
    assertEquals(0.95, req.getTopP());
  }

  @Test
  void seedField() {
    ChatRequest req = ChatRequest.builder().seed(42).build();
    assertEquals(42, req.getSeed());
  }

  @Test
  void stopField() {
    ChatRequest req = ChatRequest.builder().stop(Arrays.asList("\n", "stop")).build();
    assertEquals(Arrays.asList("\n", "stop"), req.getStop());
  }

  @Test
  void stopIsImmutable() {
    ChatRequest req = ChatRequest.builder().stop(Arrays.asList("a")).build();
    assertThrows(UnsupportedOperationException.class, () -> req.getStop().add("b"));
  }

  @Test
  void logprobsField() {
    ChatRequest req = ChatRequest.builder().logprobs(true).build();
    assertTrue(req.getLogprobs());
  }

  @Test
  void topLogprobsField() {
    ChatRequest req = ChatRequest.builder().topLogprobs(5).build();
    assertEquals(5, req.getTopLogprobs());
  }

  @Test
  void responseFormatField() {
    ChatRequest req = ChatRequest.builder().responseFormat("{\"type\":\"json_object\"}").build();
    assertEquals("{\"type\":\"json_object\"}", req.getResponseFormat());
  }

  @Test
  void jsonResponseFormatHelper() {
    String fmt = ChatRequest.jsonResponseFormat();
    assertEquals("{\"type\":\"json_object\"}", fmt);
  }

  @Test
  void textResponseFormatHelper() {
    String fmt = ChatRequest.textResponseFormat();
    assertEquals("{\"type\":\"text\"}", fmt);
  }

  @Test
  void schemaResponseFormatHelper() {
    String schema = "{\"type\":\"object\"}";
    String fmt = ChatRequest.schemaResponseFormat("my_schema", schema);
    assertTrue(fmt.contains("\"type\":\"json_schema\""));
    assertTrue(fmt.contains("\"name\":\"my_schema\""));
    assertTrue(fmt.contains("\"strict\":true"));
    assertTrue(fmt.contains("\"schema\""));
  }
}
