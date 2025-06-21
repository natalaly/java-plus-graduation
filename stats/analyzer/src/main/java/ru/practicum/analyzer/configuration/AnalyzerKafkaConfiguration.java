package ru.practicum.analyzer.configuration;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.common.kafka.KafkaConfigFactory;
import ru.practicum.common.kafka.KafkaConfigKey;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.deserializer.EventSimilarityDeserializer;
import ru.practicum.ewm.stats.deserializer.UserActionDeserializer;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.kafka")
@Configuration
@Slf4j
public class AnalyzerKafkaConfiguration {

  private String bootstrapServers;
  private Map<String, String> topics;
  private Map<String, String> groupId;

  @Bean
  public KafkaConsumer<String, UserActionAvro> kafkaUserActionsConsumer() {
    log.debug("Initializing Kafka consumer for user action group Id with bootstrap servers: {}",
        bootstrapServers);
    Properties config = KafkaConfigFactory.baseConsumerProperties(
        bootstrapServers,
        groupId.get(KafkaConfigKey.USER_ACTION.getConfigKey()),
        UserActionDeserializer.class);
    return new KafkaConsumer<>(config);
  }

  @Bean
  public KafkaConsumer<String, EventSimilarityAvro> kafkaSimilarityConsumer() {
    log.debug("Initializing Kafka consumer for Events Similarity reading with bootstrap servers: {}",
        bootstrapServers);
    Properties config = KafkaConfigFactory.baseConsumerProperties(
        bootstrapServers,
        groupId.get(KafkaConfigKey.EVENT_SIMILARITY.getConfigKey()),
        EventSimilarityDeserializer.class);
    return new KafkaConsumer<>(config);
  }

  public String getTopic(final KafkaConfigKey topicKey) {
    final String topicName = topics.get(topicKey.getConfigKey());
    if (topicName == null) {
      log.error("Topic {} not found in configuration.", topicKey.getConfigKey());
      throw new IllegalArgumentException("Undefined Kafka topic: " + topicKey.getConfigKey());
    }
    return topicName;
  }

  @PostConstruct
  private void validateConfig() {
    if (bootstrapServers == null || bootstrapServers.isBlank()) {
      throw new IllegalStateException("Missing Kafka bootstrap servers.");
    }
    if (topics == null || !topics.containsKey("user-action") || !topics.containsKey(
        "event-similarity")) {
      throw new IllegalStateException(
          "Kafka topics for 'user-action' and 'event-similarity' must be defined.");
    }
    if (groupId == null || !groupId.containsKey("user-action") || !groupId.containsKey(
        "event-similarity")) {
      throw new IllegalStateException(
          "Kafka group IDs for 'user-action' and 'event-similarity' must be defined.");
    }
    log.debug(
        "Kafka configuration validated successfully: bootstrapServers={}, topics={}, groupId={}",
        bootstrapServers, topics, groupId);
  }
}
