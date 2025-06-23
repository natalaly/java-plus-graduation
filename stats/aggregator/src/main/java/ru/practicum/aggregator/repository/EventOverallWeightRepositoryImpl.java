package ru.practicum.aggregator.repository;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class EventOverallWeightRepositoryImpl implements EventTotalWeightRepository {

  private final Map<Long, Double> eventTotalWeight = new HashMap<>();

  @Override
  public double getTotalWeight(final Long eventId) {
    double result = eventTotalWeight.getOrDefault(eventId, 0.0);
    log.trace("Retrieving total event weight for event {} = {}.", eventId, result);
    return result;
  }

  @Override
  public void increment(Long eventId, double delta) {
    eventTotalWeight.merge(eventId, delta, Double::sum);
    log.debug("Event weight for event {} updated to {}.", eventId, eventTotalWeight.get(eventId));
  }
}
