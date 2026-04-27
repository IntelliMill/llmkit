package io.llmkit.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Token-level log probability information for a response choice. */
public final class ChoiceLogprobs {

  private final List<TokenLogprob> content;

  public ChoiceLogprobs(List<TokenLogprob> content) {
    this.content =
        content != null
            ? Collections.unmodifiableList(new ArrayList<>(content))
            : Collections.<TokenLogprob>emptyList();
  }

  public List<TokenLogprob> getContent() {
    return content;
  }
}
