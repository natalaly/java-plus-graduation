package ru.practicum.aggregator.configuration;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    Properties config = new Properties();
    config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ConsumerConfig.GROUP_ID_CONFIG, consumer.getGroupId());
    config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class.getName());
    config.put("specific.avro.reader", true);
    return new KafkaConsumer<>(config);
  }

  @Bean
  public KafkaProducer<String, EventSimilarityAvro> kafkaProducer() {
    log.debug("Initializing Kafka producer with bootstrap servers: {}", bootstrapServers);
    Properties config = new Properties();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());
    return new KafkaProducer<>(config);
  }

  public String getTopic(final KafkaTopic topicKey) {
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
