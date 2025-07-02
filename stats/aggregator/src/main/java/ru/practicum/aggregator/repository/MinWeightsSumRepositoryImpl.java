package ru.practicum.aggregator.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class MinWeightsSumRepositoryImpl implements EventMinWeightSumRepository {

  private final Map<Long, Map<Long, Double>> minWeightSums = new HashMap<>();


  private static long first(long a, long b) {
    return Math.min(a, b);
  }

  private static long second(long a, long b) {
    return Math.max(a, b);
  }

  @Override
  public void incrementMinWeightSum(Long eventA, Long eventB, double delta) {
    log.trace("Updating minWeightSums matrix..");
    long first = first(eventA, eventB);
    long second = second(eventA, eventB);
    minWeightSums
        .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
        .merge(second, delta, Double::sum);
    log.trace("MinWeightSum for event {} and event {} increased by {}.", eventA, eventB, delta);
  }

  @Override
  public double getMinWeightSum(final Long eventA, final Long eventB) {
    log.trace("Retrieving minWeightSum for event A={} and event B={}.", eventA, eventB);
    long first = first(eventA, eventB);
    long second = second(eventA, eventB);
    return minWeightSums
        .computeIfAbsent(first, e -> new ConcurrentHashMap<>())
        .getOrDefault(second, 0.0);
  }
}
