package io.llmkit.internal.json;

/**
 * Minimal JSON builder for constructing API request bodies. Only handles the patterns needed for
 * OpenAI/Anthropic API requests.
 */
public final class JsonWriter {

  private final StringBuilder sb;
  private final boolean topLevel;
  private boolean firstField = true;

  public JsonWriter() {
    this.sb = new StringBuilder();
    this.topLevel = true;
    sb.append('{');
  }

  private JsonWriter(StringBuilder sb) {
    this.sb = sb;
    this.topLevel = false;
    sb.append('{');
  }

  public JsonWriter field(String key, String value) {
    if (value == null) return this;
    comma();
    sb.append('"')
        .append(escape(key))
        .append('"')
        .append(':')
        .append('"')
        .append(escape(value))
        .append('"');
    return this;
  }

  public JsonWriter field(String key, Integer value) {
    if (value == null) return this;
    comma();
    sb.append('"').append(escape(key)).append('"').append(':').append(value);
    return this;
  }

  public JsonWriter field(String key, Double value) {
    if (value == null) return this;
    comma();
    sb.append('"').append(escape(key)).append('"').append(':').append(value);
    return this;
  }

  public JsonWriter field(String key, Boolean value) {
    if (value == null) return this;
    comma();
    sb.append('"').append(escape(key)).append('"').append(':').append(value);
    return this;
  }

  public JsonWriter rawField(String key, String rawValue) {
    if (rawValue == null) return this;
    comma();
    sb.append('"').append(escape(key)).append('"').append(':').append(rawValue);
    return this;
  }

  public JsonWriter rawFieldDirect(String key, String rawValue) {
    if (rawValue == null) return this;
    comma();
    sb.append('"').append(escape(key)).append('"').append(':').append(rawValue);
    return this;
  }

  public ArrayWriter array(String key) {
    comma();
    sb.append('"').append(escape(key)).append('"').append(':').append('[');
    return new ArrayWriter(sb, this);
  }

  public String build() {
    sb.append('}');
    return sb.toString();
  }

  private void comma() {
    if (!firstField) {
      sb.append(',');
    }
    firstField = false;
  }

  private static String escape(String s) {
    if (s == null) return "";
    StringBuilder out = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"':
          out.append("\\\"");
          break;
        case '\\':
          out.append("\\\\");
          break;
        case '\n':
          out.append("\\n");
          break;
        case '\r':
          out.append("\\r");
          break;
        case '\t':
          out.append("\\t");
          break;
        default:
          if (c < 0x20) {
            out.append(String.format("\\u%04x", (int) c));
          } else {
            out.append(c);
          }
      }
    }
    return out.toString();
  }

  /** Helper for writing JSON array contents. */
  public static final class ArrayWriter {
    private final StringBuilder sb;
    private final JsonWriter parent;
    private boolean first = true;

    ArrayWriter(StringBuilder sb, JsonWriter parent) {
      this.sb = sb;
      this.parent = parent;
    }

    public ArrayWriter raw(String rawValue) {
      if (!first) sb.append(',');
      sb.append(rawValue);
      first = false;
      return this;
    }

    public ArrayWriter string(String value) {
      if (!first) sb.append(',');
      sb.append('"').append(escape(value)).append('"');
      first = false;
      return this;
    }

    public JsonWriter end() {
      sb.append(']');
      return parent;
    }
  }
}
