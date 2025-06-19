package ru.practicum.aggregator.service;

import java.util.List;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface AggregationProcessor {

  List<EventSimilarityAvro> processUserAction(UserActionAvro userAction);
}
