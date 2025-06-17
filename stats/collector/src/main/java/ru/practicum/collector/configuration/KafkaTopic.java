package ru.practicum.collector.configuration;

public enum KafkaTopic {

  USER_ACTION("user-action");

  private final String configKey;

  KafkaTopic(String configKey) {
    this.configKey = configKey;
  }

  public String getConfigKey() {
    return configKey;
  }

}
