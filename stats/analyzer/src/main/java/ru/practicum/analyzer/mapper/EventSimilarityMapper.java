package ru.practicum.analyzer.mapper;

import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@UtilityClass
@Slf4j
public class EventSimilarityMapper {

  public EventSimilarity toEntity(final EventSimilarityAvro similarity) {
    log.debug("Mapping EventSimilarityAvro {} to EventSimilarity.", similarity);
    Objects.requireNonNull(similarity);

    log.trace("Normalizing composite PK: Ensuring eventAId is always smaller value and eventBId is larger.");
    long normalizedEventAId = Math.min(similarity.getEventA(), similarity.getEventB());
    long normalizedEventBId = Math.max(similarity.getEventA(), similarity.getEventB());

    return EventSimilarity.builder()
        .eventAId(normalizedEventAId)
        .eventBId(normalizedEventBId)
        .similarityScore(similarity.getScore())
        .timestamp(similarity.getTimestamp())
        .build();
  }

}
