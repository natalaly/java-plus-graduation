package ru.practicum.aggregator.repository;

public interface EventTotalWeightRepository {

  double getTotalWeight(Long eventId);

  void increment(Long eventId, double delta);

}
