package ru.practicum.analyzer.mapper;

import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.analyzer.model.RecommendedEvent;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.model.UserActionType;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.recommendation.RecommendedEventProto;

@UtilityClass
@Slf4j
public class UserActionMapper {

  public UserAction toEntity(final UserActionAvro action) {
    log.debug("Mapping UserActionAvro {} to UserAction.", action);
    Objects.requireNonNull(action);
    return UserAction.builder()
        .userId(action.getUserId())
        .eventId(action.getEventId())
        .actionType(UserActionType.valueOf(action.getActionType().name()))
        .weight(ActionWeightMapper.toWeightValue(action.getActionType()))
        .timestamp(action.getTimestamp())
        .build();
  }

  public RecommendedEventProto toRecommendedEventProto(final RecommendedEvent recommendedEvent) {
    return RecommendedEventProto.newBuilder()
        .setEventId(recommendedEvent.getEventId())
        .setScore(recommendedEvent.getScore())
        .build();
  }
}
