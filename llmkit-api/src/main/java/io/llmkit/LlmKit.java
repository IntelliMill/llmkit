package io.llmkit;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/** Entry point facade for creating LLM clients. */
public final class LlmKit {

  private static final List<LlmProvider> PROVIDERS = loadProviders();

  private LlmKit() {}

  /**
   * Create a default client with an API key (uses the first available provider, typically OpenAI).
   */
  public static LlmClient create(String apiKey) {
    if (PROVIDERS.isEmpty()) {
      throw new IllegalStateException(
          "No LLM providers found on classpath. "
              + "Add a provider module such as llmkit-openai or llmkit-anthropic.");
    }
    return PROVIDERS.get(0).createBuilder().apiKey(apiKey).build();
  }

  /** Create a builder for a specific provider. */
  public static LlmClientBuilder builder(String providerName) {
    for (LlmProvider provider : PROVIDERS) {
      if (provider.name().equalsIgnoreCase(providerName)) {
        return provider.createBuilder();
      }
    }
    throw new IllegalArgumentException(
        "Unknown provider: " + providerName + ". Available: " + providerNames());
  }

  /** List all registered provider names. */
  public static List<String> providers() {
    List<String> names = new ArrayList<>();
    for (LlmProvider provider : PROVIDERS) {
      names.add(provider.name());
    }
    return names;
  }

  private static List<LlmProvider> loadProviders() {
    List<LlmProvider> providers = new ArrayList<>();
    ServiceLoader<LlmProvider> loader = ServiceLoader.load(LlmProvider.class);
    for (LlmProvider provider : loader) {
      providers.add(provider);
    }
    return providers;
  }

  private static List<String> providerNames() {
    List<String> names = new ArrayList<>();
    for (LlmProvider provider : PROVIDERS) {
      names.add(provider.name());
    }
    return names;
  }
}
