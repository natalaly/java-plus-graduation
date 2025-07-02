package ru.practicum.analyzer.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface EventSimilarityProcessor {

  void process(EventSimilarityAvro value);
}
