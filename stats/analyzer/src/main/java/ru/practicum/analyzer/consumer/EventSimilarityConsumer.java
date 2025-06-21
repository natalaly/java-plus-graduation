package ru.practicum.analyzer.consumer;

import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.configuration.AnalyzerKafkaConfiguration;
import ru.practicum.analyzer.service.EventSimilarityProcessor;
import ru.practicum.common.kafka.KafkaConfigKey;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
@Slf4j
public class EventSimilarityConsumer implements Runnable {

  private static final Duration POLL_TIMEOUT = Duration.ofMillis(1000);
  private final KafkaConsumer<String, EventSimilarityAvro> kafkaSimilarityConsumer;
  private final List<String> topics;
  private final EventSimilarityProcessor eventProcessor;
  private volatile boolean stopped = false;

  public EventSimilarityConsumer(final KafkaConsumer<String, EventSimilarityAvro> kafkaSimilarityConsumer,
                                 final EventSimilarityProcessor eventProcessor,
                                 final AnalyzerKafkaConfiguration configs) {
    this.kafkaSimilarityConsumer = kafkaSimilarityConsumer;
    this.topics = List.of(configs.getTopic(KafkaConfigKey.EVENT_SIMILARITY));
    this.eventProcessor = eventProcessor;
  }

  @Override
  public void run() {
    log.info("Starting listening for User Action messages.");
    Runtime.getRuntime().addShutdownHook(new Thread(kafkaSimilarityConsumer::wakeup));
    try {
      kafkaSimilarityConsumer.subscribe(topics);
      log.debug("Consumer {} subscribed for the topics {}.", kafkaSimilarityConsumer.getClass().getName(), topics);

      pollLoop();

    } catch (WakeupException ignores) {
      log.warn("WakeupException caught - Consumer shutting down.");
    } catch (Exception e) {
      log.error("Error during event processing", e);
    } finally {
      cleanupResources();
    }

  }

  private void pollLoop() {
    while (!stopped) {
      ConsumerRecords<String, EventSimilarityAvro> records =
          kafkaSimilarityConsumer.poll(POLL_TIMEOUT);
      if (!records.isEmpty()) {
        processRecords(records);
        doCommitOffsets();
      }
    }
  }

  private void processRecords(final ConsumerRecords<String, EventSimilarityAvro> records) {
    log.info("Processing {} records from the Kafka.", records.count());
    for (ConsumerRecord<String, EventSimilarityAvro> record : records) {
      eventProcessor.process(record.value());
    }
  }

  private void doCommitOffsets() {
    try {
      kafkaSimilarityConsumer.commitSync();
      log.debug("Offsets committed successfully.");
    } catch (Exception e) {
      log.warn("Error during committing consumer offsets", e);
    }
  }

  private void cleanupResources() {
    try {
      kafkaSimilarityConsumer.commitSync();
    } catch (Exception e) {
      log.error("Error during resource cleanup", e);
    } finally {
      log.info("Closing Kafka consumer {}.", kafkaSimilarityConsumer.getClass().getName());
      kafkaSimilarityConsumer.close();
    }
  }
}
