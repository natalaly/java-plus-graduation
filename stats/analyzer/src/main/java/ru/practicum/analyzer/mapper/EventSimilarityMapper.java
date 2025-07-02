package ru.practicum.analyzer.mapper;

import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.recommendation.RecommendedEventProto;

@UtilityClass
@Slf4j
public class EventSimilarityMapper {

  public EventSimilarity toEntity(final EventSimilarityAvro similarity) {
    log.debug("Mapping EventSimilarityAvro {} to EventSimilarity.", similarity);
    Objects.requireNonNull(similarity);

    log.trace(
        "Normalizing composite PK: Ensuring eventAId is always smaller value and eventBId is larger.");
    long normalizedEventAId = Math.min(similarity.getEventA(), similarity.getEventB());
    long normalizedEventBId = Math.max(similarity.getEventA(), similarity.getEventB());

    return EventSimilarity.builder()
        .eventAId(normalizedEventAId)
        .eventBId(normalizedEventBId)
        .similarityScore(similarity.getScore())
        .timestamp(similarity.getTimestamp())
        .build();
  }

  public List<RecommendedEventProto> mapToRecommendedEventProto(final Long baseEventId,
                                                                final List<EventSimilarity> filteredSortedNew) {
    final List<RecommendedEventProto> result = filteredSortedNew.stream()
        .map(sim -> {
          Long recommendedEventId = sim.getEventAId().equals(baseEventId)
              ? sim.getEventBId()
              : sim.getEventAId();
          return RecommendedEventProto.newBuilder()
              .setEventId(recommendedEventId)
              .setScore(sim.getSimilarityScore())
              .build();
        })
        .toList();
    log.trace("Returning {} recommendations for eventId={}.", result.size(), baseEventId);
    return result;
  }
}
