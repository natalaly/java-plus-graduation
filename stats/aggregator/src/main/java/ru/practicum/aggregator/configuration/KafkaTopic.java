package ru.practicum.aggregator.configuration;

public enum KafkaTopic {

  USER_ACTION("user-action"),
  EVENT_SIMILARITY("event-similarity");

  private final String configKey;

  KafkaTopic(String configKey) {
    this.configKey = configKey;
  }

  public String getConfigKey() {
    return configKey;
  }
}
