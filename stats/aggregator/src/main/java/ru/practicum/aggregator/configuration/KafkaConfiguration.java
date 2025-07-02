package ru.practicum.aggregator.configuration;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.common.kafka.KafkaConfigFactory;
import ru.practicum.common.kafka.KafkaConfigKey;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.deserializer.UserActionDeserializer;
import ru.practicum.ewm.stats.serializer.GeneralAvroSerializer;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.kafka")
@Configuration
@Slf4j
public class KafkaConfiguration {

  private String bootstrapServers;
  private Map<String, String> topics;
  private ConsumerProperties consumer;

  @Bean
  public KafkaConsumer<String, UserActionAvro> kafkaConsumer() {
    log.debug("Initializing Kafka consumer with bootstrap servers: {}", bootstrapServers);

    Properties config = KafkaConfigFactory.baseConsumerProperties(
        bootstrapServers,
        consumer.getGroupId(),
        UserActionDeserializer.class
    );

    return new KafkaConsumer<>(config);
  }

  @Bean
  public KafkaProducer<String, EventSimilarityAvro> kafkaProducer() {
    log.debug("Initializing Kafka producer with bootstrap servers: {}", bootstrapServers);

    Properties config = KafkaConfigFactory.baseProducerProperties(
        bootstrapServers,
        GeneralAvroSerializer.class
    );

    return new KafkaProducer<>(config);
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
    if (bootstrapServers == null || topics == null || topics.isEmpty()) {
      log.error("Invalid Kafka configuration. Missing bootstrapServers or topics.");
      throw new IllegalStateException("Missing required Kafka configuration.");
    }
    if (consumer == null ||
        consumer.getGroupId() == null || consumer.getGroupId().isBlank() ||
        consumer.getAutoOffsetReset() == null || consumer.getAutoOffsetReset().isBlank()) {
      log.error("Invalid Kafka consumer configuration: {}", consumer);
      throw new IllegalStateException("Missing required Kafka consumer configuration (groupId or autoOffsetReset).");
    }

    log.debug("Kafka configuration validated successfully: bootstrapServers={}, topics={}, groupId={}, autoOffsetReset={}",
        bootstrapServers, topics, consumer.getGroupId(), consumer.getAutoOffsetReset());
  }

  @Getter
  @Setter
  public static class ConsumerProperties {
    private String groupId;
    private String autoOffsetReset;
  }
}
