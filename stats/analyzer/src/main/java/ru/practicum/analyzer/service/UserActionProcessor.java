package ru.practicum.analyzer.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface UserActionProcessor {

  void process(UserActionAvro value);
}
