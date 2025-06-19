package ru.practicum.aggregator.repository;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class EventUserActionWeightMatrixRepositoryImpl implements EventUserWeightMatrixRepository {

  /**
   * Interaction weights matrix:: eventID -> (userID -> weight)
   */
  private final Map<Long, Map<Long, Double>> eventToUserWeightMatrix = new ConcurrentHashMap<>();

  /**
   * 'Index' for easy search, eventsIDs grouped by usersIDs: userID -> (eventIDs)
   */
  private final Map<Long, Set<Long>> userToEventIds = new ConcurrentHashMap<>();

  @Override
  public Double getUserWeight(final Long eventId, final Long userId) {
    final Map<Long, Double> userWeights = eventToUserWeightMatrix.get(eventId);
    if (userWeights == null) {
      log.debug("No records found for event {}.", eventId);
      return null;
    }
    return userWeights.getOrDefault(userId, 0.0);
  }

  @Override
  public void putUserWeight(final Long eventId, final Long userId, final double weight) {
    log.trace("Updating weights matrix..");
    eventToUserWeightMatrix
        .computeIfAbsent(eventId, e -> new ConcurrentHashMap<>())
        .put(userId, weight);
    userToEventIds.computeIfAbsent(userId, u -> ConcurrentHashMap.newKeySet())
        .add(eventId);
    log.trace(
        "Reassuring  interaction userID - eventsID registered. Added event {} to user {} into eventUSerIds map: {}.",
        eventId, userId, userToEventIds.get(userId));
    log.debug("User weight for event {} and user {} updated to {}.", eventId, userId, weight);
  }

  @Override
  public Set<Long> getEventIdsByUser(final Long userId, final Long exclude) {
    return userToEventIds.getOrDefault(userId, Collections.emptySet())
        .stream()
        .filter(eventId -> !eventId.equals(exclude))
        .collect(Collectors.toSet());
  }

  @Override
  public boolean doesEventExists(Long eventId) {
    return eventToUserWeightMatrix.containsKey(eventId);
  }
}
