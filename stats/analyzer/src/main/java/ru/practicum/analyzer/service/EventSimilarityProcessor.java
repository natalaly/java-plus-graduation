package ru.practicum.analyzer.service;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface EventSimilarityProcessor {


  void process(EventSimilarityAvro value);
}
