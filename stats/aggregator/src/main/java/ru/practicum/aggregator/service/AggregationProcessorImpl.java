package ru.practicum.aggregator.service;

import static java.lang.Math.sqrt;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.aggregator.repository.EventMinWeightSumRepository;
import ru.practicum.aggregator.repository.EventTotalWeightRepository;
import ru.practicum.aggregator.repository.EventUserWeightMatrixRepository;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationProcessorImpl implements AggregationProcessor {

  private final EventTotalWeightRepository eventTotalWeightRepository;
  private final EventUserWeightMatrixRepository eventUserWeightMatrixRepository;
  private final EventMinWeightSumRepository eventPairMinWeightSumRepository;

  @Override
  public List<EventSimilarityAvro> processUserAction(final UserActionAvro userAction) {

    final Long eventId = userAction.getEventId();
    final Long userId = userAction.getUserId();
    final Double newWeight = resolveWeight(userAction.getActionType());

    log.debug("Starting processing User Action. EventID={}, USerID={}, ActioType={}.",
        eventId, userId, userAction.getActionType());

    final Double oldWeight = eventUserWeightMatrixRepository.getUserWeight(eventId, userId);
    boolean isNewInteraction = (oldWeight == null);

    if (!isNewInteraction && oldWeight >= newWeight) {
      log.debug(
          "Weight from user {} for event {} has not increased (oldWeight={}, newWeight={}). Skipping.",
          userId, eventId, oldWeight, newWeight);
      return List.of();
    }

    final double deltaWeight = isNewInteraction ? newWeight : newWeight - oldWeight;
    log.debug("Updating event weights: deltaWeight={} (oldWeight={}, newWeight={})",
        deltaWeight, oldWeight, newWeight);
    eventTotalWeightRepository.increment(eventId, deltaWeight);
    eventUserWeightMatrixRepository.putUserWeight(eventId, userId, newWeight);

    return computeSimilarities(eventId, userId, newWeight, isNewInteraction ? 0.0 : oldWeight,
        userAction.getTimestamp());
  }

  private List<EventSimilarityAvro> computeSimilarities(final Long eventIdA,
                                                        final long userId,
                                                        final double newWeight,
                                                        final Double oldWeight,
                                                        final Instant timestamp) {
    log.debug("Computing similarities for event {} and user {}. OldWeight={}, NewWeight={}",
        eventIdA, userId, oldWeight, newWeight);
    log.trace("Getting all events IDs, which user(ID={}) interacted with..", userId);

    final Set<Long> allEvents = eventUserWeightMatrixRepository.getEventIdsByUser(userId, eventIdA);

    if (allEvents.isEmpty()) {
      log.debug("No related events found for user {}, returning empty similarity list.", userId);
      return List.of();
    }
    log.trace("Found {} events to recalculate similarities .", allEvents.size());

    final List<EventSimilarityAvro> similarities = new ArrayList<>();

    double totalWeightA = eventTotalWeightRepository.getTotalWeight(eventIdA);

    for (Long eventIdB : allEvents) {
      final double similarity = computeSimilarityForPair(eventIdA, eventIdB, userId, newWeight,
          oldWeight, totalWeightA);
      if (similarity >= 0) {
        similarities.add(buildSimilarityAvro(eventIdA, eventIdB, similarity, timestamp));
      }
    }
    return similarities;
  }

  private double computeSimilarityForPair(final Long eventIdA,
                                          final Long eventIdB,
                                          final long userId,
                                          final double newWeight,
                                          final double oldWeight,
                                          final double totalWeightA) {
    log.trace("Recalculating similarity for event A ID={} and event B ID={}.", eventIdA, eventIdB);

    final double weightB = eventUserWeightMatrixRepository.getUserWeight(eventIdB, userId);
    if (weightB <= 0) {
      log.trace("User {} has no weight for event {}. Skipping similarity calculation.", userId,
          eventIdB);
      return -1;
    }

    final double oldMin = Math.min(oldWeight, weightB);
    final double newMin = Math.min(newWeight, weightB);
    final double deltaMin = newMin - oldMin;

    if (deltaMin != 0) {
      eventPairMinWeightSumRepository.incrementMinWeightSum(eventIdA, eventIdB, deltaMin);
    } else {
      log.trace("Delta min for events ({}, {}) is zero; skipping min sum update.", eventIdA,
          eventIdB);
    }

    final double totalWeightB = eventTotalWeightRepository.getTotalWeight(eventIdB);
    final double updatedMinSum = eventPairMinWeightSumRepository.getMinWeightSum(eventIdA,
        eventIdB);

    if (totalWeightA <= 0 || totalWeightB <= 0) {
      log.warn("Total weight zero for event pair ({}, {}), skipping similarity.", eventIdA,
          eventIdB);
      return -1;
    }

    return updatedMinSum / sqrt(totalWeightA * totalWeightB);
  }

  private EventSimilarityAvro buildSimilarityAvro(long eventIdA, long eventIdB, double score,
                                                  final Instant timestamp) {
    long first = Math.min(eventIdA, eventIdB);
    long second = Math.max(eventIdA, eventIdB);
    return EventSimilarityAvro.newBuilder()
        .setEventA(first)
        .setEventB(second)
        .setScore((float) score)
        .setTimestamp(timestamp)
        .build();
  }

  private Double resolveWeight(final ActionTypeAvro actionType) {
    log.debug("Resolving weight for the event by action type:{}.", actionType);
    return switch (actionType) {
      case VIEW -> 0.4;
      case REGISTER -> 0.8;
      case LIKE -> 1.0;
      default -> throw new IllegalStateException(
          "Unexpected value: " + actionType);
    };
  }
}
