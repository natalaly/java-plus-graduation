package ru.practicum.common.kafka;

public enum KafkaConfigKey {

  USER_ACTION("user-action"),
  EVENT_SIMILARITY("event-similarity");

  private final String configKey;

  KafkaConfigKey(String configKey) {
    this.configKey = configKey;
  }

  public String getConfigKey() {
    return configKey;
  }
}

