package ru.practicum.collector.configuration;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.serializer.GeneralAvroSerializer;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.kafka")
@Configuration
@Slf4j
public class KafkaConfiguration {

  private String bootstrapServers;
  private Map<String, String> topics;
  private Map<String, String> properties;

  @Bean
  public Producer<String, SpecificRecordBase> kafkaProducer() {
    log.debug("Initializing Kafka producer with bootstrap servers: {}", bootstrapServers);
    Properties config = new Properties();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, GeneralAvroSerializer.class.getName());
    if (properties != null) {
      config.putAll(properties);
    }
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
      log.error("Invalid Kafka configuration. Missing bootstrapServers, topics.");
      throw new IllegalStateException("Missing required Kafka configuration.");
    }
    log.debug("Kafka configuration validated successfully: bootstrapServers={}, topics={}.",
        bootstrapServers, topics);
  }
}
