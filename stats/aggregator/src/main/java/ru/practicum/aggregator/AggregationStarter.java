package ru.practicum.aggregator;

import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.configuration.KafkaConfiguration;
import ru.practicum.aggregator.configuration.KafkaTopic;
import ru.practicum.aggregator.service.AggregationProcessor;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@Slf4j
public class AggregationStarter {

  private static final Duration POLL_TIMEOUT = Duration.ofMillis(1000);
  private final Producer<String, EventSimilarityAvro> producer;
  private final Consumer<String, UserActionAvro> consumer;
  private final AggregationProcessor processor;
  private final List<String> userActionTopics;
  private final String eventsTopics;

  public AggregationStarter(final Producer<String, EventSimilarityAvro> producer,
                            final Consumer<String, UserActionAvro> consumer,
                            final AggregationProcessor processor,
                            final KafkaConfiguration config) {
    this.producer = producer;
    this.consumer = consumer;
    this.processor = processor;
    this.userActionTopics = List.of(config.getTopic(KafkaTopic.USER_ACTION));
    this.eventsTopics = config.getTopic(KafkaTopic.EVENT_SIMILARITY);
  }

  public void start() {
    log.info("Starting aggregation process.");
    try {
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        log.warn("Shutdown hook triggered. Waking up Kafka consumer.");
        consumer.wakeup();
      }));

      consumer.subscribe(userActionTopics);
      log.debug("Subscribed for the topic: {}", userActionTopics);

      pollLoop();

    } catch (WakeupException ignored) {
      log.warn("Kafka consumer wakeup triggered. Exiting polling loop.");
    } catch (Exception e) {
      log.error("Unexpected error during event processing.", e);
    } finally {
      cleanupResources();
    }

  }

  private void pollLoop() {
    while (true) {
      ConsumerRecords<String, UserActionAvro> records = consumer.poll(POLL_TIMEOUT);
      log.debug("Polled {} records", records.count());

      if (!records.isEmpty()) {
        processRecords(records);
        doCommitOffsets();
      }
    }
  }

  private void processRecords(final ConsumerRecords<String, UserActionAvro> records) {
    log.info("Processing {} records from the Kafka.", records.count());
    for (ConsumerRecord<String, UserActionAvro> record : records) {
      processRecord(record);
    }
  }

  private void processRecord(final ConsumerRecord<String, UserActionAvro> record) {
    log.info(
        "Processing  ConsumerRecord: topic={}, partition={}, offset={}, hubId={}, timestamp={}",
        record.topic(), record.partition(), record.offset(), record.key(), record.timestamp());

    final List<EventSimilarityAvro> similarities = processor.processUserAction(record.value());
    log.debug("Processor returned {} similarity record(s).", similarities.size());
    similarities.forEach(this::sendSimilarityToKafka);
  }

  private void sendSimilarityToKafka(final EventSimilarityAvro similarityAvro) {
    log.info("Sending similarity result to Kafka: key=null, value={}", similarityAvro);

    final ProducerRecord<String, EventSimilarityAvro> record =
        new ProducerRecord<>(eventsTopics, null, similarityAvro);

    producer.send(record, (metadata, exception) -> {
      if (exception != null) {
        log.error("Failed to send similarity result to Kafka", exception);
      } else {
        log.info("Similarity result sent successfully to the topic {} at offset {}",
            metadata.topic(),
            metadata.offset());
      }
    });
  }

  private void doCommitOffsets() {
    try {
      consumer.commitSync();
      log.debug("Consumer offsets committed successfully.");
    } catch (Exception e) {
      log.error("Failed to commit consumer offsets.", e);
    }
  }

  private void cleanupResources() {
    try {
      log.info("Flushing producer buffer.");
      producer.flush();

      log.info("Committing consumer offsets synchronously.");
      consumer.commitSync();
    } catch (Exception e) {
      log.error("Error during resource cleanup", e);
    } finally {
      log.info("Closing Kafka consumer.");
      consumer.close();
      log.info("Closing Kafka producer.");
      producer.close();
    }
  }
}
