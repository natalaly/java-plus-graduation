package ru.practicum.api;

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.EventFullDto;

public interface EventOperations {

  @GetMapping("/{eventId}")
  EventFullDto getEvent(@PathVariable("eventId") @Positive Long eventId);

  @GetMapping("/{eventId}/exists")
  boolean existsById(@PathVariable("eventId") @Positive Long eventId);
}
