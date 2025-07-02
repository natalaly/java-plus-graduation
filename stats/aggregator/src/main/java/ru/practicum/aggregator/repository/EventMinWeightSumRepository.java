package ru.practicum.aggregator.repository;

public interface EventMinWeightSumRepository {

  double getMinWeightSum(Long eventIdA, Long eventIdB);

  void incrementMinWeightSum(Long eventIdA, Long eventIdB, double delta);

}
