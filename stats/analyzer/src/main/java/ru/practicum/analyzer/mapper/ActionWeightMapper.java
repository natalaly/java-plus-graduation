package ru.practicum.analyzer.mapper;

import java.util.Objects;
import lombok.experimental.UtilityClass;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;

@UtilityClass
public class ActionWeightMapper {

  public double toWeight(final ActionTypeAvro type) {
    Objects.requireNonNull(type);
    return switch (type) {
      case VIEW -> 0.4;
      case REGISTER -> 0.8;
      case LIKE -> 1.0;
    };
  }
}
