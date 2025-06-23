package ru.practicum.collector.handler;

import java.time.Instant;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.collector.configuration.KafkaConfiguration;
import ru.practicum.collector.producer.KafkaActionProducer;
import ru.practicum.common.kafka.KafkaConfigKey;
import ru.practicum.ewm.stats.action.ActionTypeProto;
import ru.practicum.ewm.stats.action.UserActionProto;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@Slf4j
public class UserActionHandlerImpl implements UserActionHandler {

  private final KafkaActionProducer producer;
  private final String userActionsTopic;

  public UserActionHandlerImpl(final KafkaActionProducer producer, final KafkaConfiguration config) {
    this.producer = producer;
    this.userActionsTopic = config.getTopic(KafkaConfigKey.USER_ACTION);
  }


  @Override
  public void handle(final UserActionProto userActionProto) {
    log.debug("Handling user action: userID={}, eventID={}, actionType={}.",
        userActionProto.getUserId(), userActionProto.getEventId(), userActionProto.getActionType());
    Objects.requireNonNull(userActionProto);

    final UserActionAvro userActionAvro = mapToAvro(userActionProto);

    producer.send(userActionsTopic, userActionAvro);
    log.info("User action successfully sent to the topic '{}'.", userActionsTopic);
  }

  private UserActionAvro mapToAvro(final UserActionProto userActionProto) {
    log.debug("Mapping UserActionProto {} to UserActionAvro.", userActionProto);
    final UserActionAvro avro = UserActionAvro.newBuilder()
        .setUserId(userActionProto.getUserId())
        .setEventId(userActionProto.getEventId())
        .setActionType(mapToAvro(userActionProto.getActionType()))
        .setTimestamp(Instant.ofEpochSecond(
            userActionProto.getTimestamp().getSeconds(),
            userActionProto.getTimestamp().getNanos()))
        .build();
    log.debug("Mapped UserActionProto into UserActionAvro: {}.", avro);
    return avro;
  }

  private ActionTypeAvro mapToAvro(final ActionTypeProto type) {
    return switch (type) {
      case ACTION_LIKE -> ActionTypeAvro.LIKE;
      case ACTION_VIEW -> ActionTypeAvro.VIEW;
      case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
      default -> throw new IllegalStateException(
          "Unexpected value: " + type);
    };
  }
}
