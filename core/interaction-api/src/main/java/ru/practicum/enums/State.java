package ru.practicum.enums;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum State {

  PENDING,
  PUBLISHED,
  CANCELED;

  public static State fromString(final String eventState) {
    Objects.requireNonNull(eventState, "Invalid stateAction value.");
    for (State state : State.values()) {
      if (state.name().equalsIgnoreCase(eventState.trim())) {
        return state;
      }
    }
    log.warn("Invalid state value: {}", eventState);
    throw new IllegalArgumentException("Invalid state value.");
  }

}
