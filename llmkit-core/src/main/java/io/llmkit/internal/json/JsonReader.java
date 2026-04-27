package io.llmkit.internal.json;

/**
 * Minimal JSON parser for fixed-format API responses. Only handles the patterns produced by
 * OpenAI/Anthropic APIs.
 */
public final class JsonReader {

  private final String json;
  private int pos;

  public JsonReader(String json) {
    this.json = json;
    this.pos = 0;
  }

  /** Read the next string value for a given key pattern: "key":"value" or "key": "value" */
  public String readString(String key) {
    String pattern = "\"" + key + "\"";
    int idx = json.indexOf(pattern, pos);
    if (idx < 0) {
      idx = json.indexOf(pattern);
      if (idx < 0) return null;
    }
    // Move past the key and colon
    int colonIdx = json.indexOf(':', idx + pattern.length());
    if (colonIdx < 0) return null;

    // Skip whitespace
    int valueStart = colonIdx + 1;
    while (valueStart < json.length() && json.charAt(valueStart) == ' ') {
      valueStart++;
    }

    if (valueStart >= json.length() || json.charAt(valueStart) != '"') {
      return null;
    }

    return readQuotedString(valueStart + 1);
  }

  /** Read a numeric value for a given key. */
  public Integer readInt(String key) {
    String pattern = "\"" + key + "\"";
    int idx = json.indexOf(pattern, pos);
    if (idx < 0) {
      idx = json.indexOf(pattern);
      if (idx < 0) return null;
    }
    int colonIdx = json.indexOf(':', idx + pattern.length());
    if (colonIdx < 0) return null;

    int valueStart = colonIdx + 1;
    while (valueStart < json.length() && json.charAt(valueStart) == ' ') {
      valueStart++;
    }

    int valueEnd = valueStart;
    if (valueEnd < json.length() && json.charAt(valueEnd) == '-') {
      valueEnd++;
    }
    while (valueEnd < json.length() && Character.isDigit(json.charAt(valueEnd))) {
      valueEnd++;
    }
    if (valueEnd == valueStart) return null;
    return Integer.parseInt(json.substring(valueStart, valueEnd));
  }

  /** Find the start position of a JSON array for a given key. */
  public int findArray(String key) {
    String pattern = "\"" + key + "\"";
    int idx = json.indexOf(pattern, pos);
    if (idx < 0) {
      idx = json.indexOf(pattern);
      if (idx < 0) return -1;
    }
    int colonIdx = json.indexOf(':', idx + pattern.length());
    if (colonIdx < 0) return -1;

    int arrStart = colonIdx + 1;
    while (arrStart < json.length() && json.charAt(arrStart) == ' ') {
      arrStart++;
    }
    if (arrStart >= json.length() || json.charAt(arrStart) != '[') {
      return -1;
    }
    return arrStart + 1;
  }

  /** Find the bounds of the object at the current position. Returns [start, end) indices. */
  public int[] findObjectBounds(int from) {
    if (from >= json.length() || json.charAt(from) != '{') {
      // skip whitespace
      while (from < json.length()
          && (json.charAt(from) == ' '
              || json.charAt(from) == '\n'
              || json.charAt(from) == '\r'
              || json.charAt(from) == '\t')) {
        from++;
      }
      if (from >= json.length() || json.charAt(from) != '{') return null;
    }
    int start = from;
    int depth = 1;
    int i = from + 1;
    boolean inString = false;
    while (i < json.length() && depth > 0) {
      char c = json.charAt(i);
      if (inString) {
        if (c == '\\' && i + 1 < json.length()) {
          i += 2;
          continue;
        }
        if (c == '"') inString = false;
      } else {
        if (c == '"') inString = true;
        else if (c == '{') depth++;
        else if (c == '}') depth--;
      }
      i++;
    }
    return new int[] {start, i};
  }

  /** Create a new reader for a substring. */
  public JsonReader subReader(int start, int end) {
    return new JsonReader(json.substring(start, end));
  }

  /** Move position past a given index. */
  public void setPosition(int position) {
    this.pos = position;
  }

  public int getPosition() {
    return pos;
  }

  public String getJson() {
    return json;
  }

  /** Read a floating-point value for a given key. */
  public Double readDouble(String key) {
    String pattern = "\"" + key + "\"";
    int idx = json.indexOf(pattern, pos);
    if (idx < 0) {
      idx = json.indexOf(pattern);
      if (idx < 0) return null;
    }
    int colonIdx = json.indexOf(':', idx + pattern.length());
    if (colonIdx < 0) return null;

    int valueStart = colonIdx + 1;
    while (valueStart < json.length() && json.charAt(valueStart) == ' ') {
      valueStart++;
    }

    int valueEnd = valueStart;
    if (valueEnd < json.length() && json.charAt(valueEnd) == '-') {
      valueEnd++;
    }
    boolean seenDot = false;
    boolean seenExp = false;
    while (valueEnd < json.length()) {
      char c = json.charAt(valueEnd);
      if (Character.isDigit(c)) {
        valueEnd++;
      } else if (c == '.' && !seenDot && !seenExp) {
        seenDot = true;
        valueEnd++;
      } else if ((c == 'e' || c == 'E') && !seenExp) {
        seenExp = true;
        valueEnd++;
        if (valueEnd < json.length()
            && (json.charAt(valueEnd) == '+' || json.charAt(valueEnd) == '-')) {
          valueEnd++;
        }
      } else {
        break;
      }
    }
    if (valueEnd == valueStart) return null;
    try {
      return Double.parseDouble(json.substring(valueStart, valueEnd));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** Read a boolean value for a given key. */
  public Boolean readBoolean(String key) {
    String pattern = "\"" + key + "\"";
    int idx = json.indexOf(pattern, pos);
    if (idx < 0) {
      idx = json.indexOf(pattern);
      if (idx < 0) return null;
    }
    int colonIdx = json.indexOf(':', idx + pattern.length());
    if (colonIdx < 0) return null;

    int valueStart = colonIdx + 1;
    while (valueStart < json.length() && json.charAt(valueStart) == ' ') {
      valueStart++;
    }

    if (json.startsWith("true", valueStart)) {
      return Boolean.TRUE;
    } else if (json.startsWith("false", valueStart)) {
      return Boolean.FALSE;
    }
    return null;
  }

  private String readQuotedString(int start) {
    StringBuilder sb = new StringBuilder();
    int i = start;
    while (i < json.length()) {
      char c = json.charAt(i);
      if (c == '\\' && i + 1 < json.length()) {
        char next = json.charAt(i + 1);
        switch (next) {
          case '"':
            sb.append('"');
            break;
          case '\\':
            sb.append('\\');
            break;
          case '/':
            sb.append('/');
            break;
          case 'n':
            sb.append('\n');
            break;
          case 'r':
            sb.append('\r');
            break;
          case 't':
            sb.append('\t');
            break;
          case 'u':
            if (i + 5 < json.length()) {
              String hex = json.substring(i + 2, i + 6);
              sb.append((char) Integer.parseInt(hex, 16));
              i += 4;
            }
            break;
          default:
            sb.append(next);
        }
        i += 2;
      } else if (c == '"') {
        pos = i + 1;
        return sb.toString();
      } else {
        sb.append(c);
        i++;
      }
    }
    pos = i;
    return sb.toString();
  }
}
