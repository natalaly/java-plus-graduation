package ru.practicum.collector.producer;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@Slf4j
@Getter
@Setter
@RequiredArgsConstructor
public class KafkaActionProducer implements AutoCloseable {

  private final Producer<String, SpecificRecordBase> producer;

  public void send(final String topic, final UserActionAvro action) {
    log.info("Starting sending User-Action message to Kafka. Topic: {}, user-action: {}.",
        topic, action);
    try {
      final ProducerRecord<String, SpecificRecordBase> record =
          new ProducerRecord<>(topic,
              null,
              action.getTimestamp().toEpochMilli(),
              null,
              action);

      producer.send(record, (metadata, exception) -> {
        if (exception != null) {
          log.error("Failed to send message to Kafka. Topic: {}, Key: {}, Error: {}",
              topic, action.getEventId(), exception.getMessage(), exception);
        } else {
          log.info("Message sent successfully. Topic: {}, Partition: {}, Offset: {}, Timestamp: {}",
              metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp());
        }
      });
    } catch (Exception ex) {
      log.error("Unexpected error occurred during message sending to Kafka.", ex);
      throw new RuntimeException("Error during Kafka message sending.", ex);
    }

  }

  @PreDestroy
  @Override
  public void close() throws Exception {
    try {
      log.info("Flushing producer buffer.");
      producer.flush();
      log.info("Closing Kafka producer.");
      producer.close();
    } catch (Exception e) {
      log.error("Error during closing Kafka producer", e);
    }
  }
}
