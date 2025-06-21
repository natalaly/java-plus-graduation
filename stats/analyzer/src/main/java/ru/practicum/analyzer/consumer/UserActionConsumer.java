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
import ru.practicum.analyzer.service.UserActionProcessor;
import ru.practicum.common.kafka.KafkaConfigKey;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@Slf4j
public class UserActionConsumer implements Runnable {

  private static final Duration POLL_TIMEOUT = Duration.ofMillis(1000);
  private final KafkaConsumer<String, UserActionAvro> kafkaUserActionsConsumer;
  private final List<String> topics;
  private final UserActionProcessor userActionProcessor;
  private volatile boolean stopped = false;

  public UserActionConsumer(final KafkaConsumer<String, UserActionAvro> kafkaUserActionsConsumer,
                            final UserActionProcessor userActionProcessor,
                            final AnalyzerKafkaConfiguration configs) {
    this.kafkaUserActionsConsumer = kafkaUserActionsConsumer;
    this.topics = List.of(configs.getTopic(KafkaConfigKey.USER_ACTION));
    this.userActionProcessor = userActionProcessor;
  }

  @Override
  public void run() {
    log.info("Starting listening for User Action messages.");
    Runtime.getRuntime().addShutdownHook(new Thread(kafkaUserActionsConsumer::wakeup));
    try {
      kafkaUserActionsConsumer.subscribe(topics);
      log.debug("Consumer {} subscribed for the topics {}.", kafkaUserActionsConsumer.getClass().getName(), topics);

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
      ConsumerRecords<String, UserActionAvro> actionRecords =
          kafkaUserActionsConsumer.poll(POLL_TIMEOUT);
      if (!actionRecords.isEmpty()) {
        processRecords(actionRecords);
        doCommitOffsets();
      }
    }
  }

  private void processRecords(final ConsumerRecords<String, UserActionAvro> actionRecords) {
    log.info("Processing {} records from the Kafka.", actionRecords.count());
    for (ConsumerRecord<String, UserActionAvro> record : actionRecords) {
      userActionProcessor.process(record.value());
    }
  }

  private void doCommitOffsets() {
    try {
      kafkaUserActionsConsumer.commitSync();
      log.debug("Offsets committed successfully.");
    } catch (Exception e) {
      log.warn("Error during committing consumer offsets", e);
    }
  }

  private void cleanupResources() {
    try {
      kafkaUserActionsConsumer.commitSync();
    } catch (Exception e) {
      log.error("Error during resource cleanup", e);
    } finally {
      log.info("Closing Kafka consumer {}.", kafkaUserActionsConsumer.getClass().getName());
      kafkaUserActionsConsumer.close();
    }
  }
}
