package ru.practicum.aggregator.repository;

import java.util.Set;

public interface EventUserWeightMatrixRepository {

  Double getUserWeight(Long eventId, Long userId);

  void putUserWeight(Long eventId, Long userId, double weight);

  Set<Long> getEventIdsByUser(Long userId, Long exclude);

}
