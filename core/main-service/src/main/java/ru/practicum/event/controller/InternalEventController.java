package ru.practicum.event.controller;


import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.EventOperations;
import ru.practicum.dto.EventFullDto;
import ru.practicum.event.service.EventProcessingService;

@RestController
@RequestMapping("/internal/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class InternalEventController implements EventOperations {

  private final EventProcessingService eventService;

  @Override
  @GetMapping("/{eventId}")
  public EventFullDto getEvent(@PathVariable("eventId") @Positive Long eventId) {
    log.info("Request received GET /internal/events/{}", eventId);
    final EventFullDto event = eventService.getEvent(eventId);
    log.info("Sending event ID={} data.", event.getId());
    return event;
  }

  @Override
  @GetMapping("/{eventId}/exists")
  public boolean existsById(@PathVariable("eventId") @Positive Long eventId) {
    log.info("Received request to check if event with id {} exists.", eventId);
    final boolean eventExist = eventService.eventExists(eventId);
    log.info("Returning response: event ID {} exist: {}", eventId, eventExist);
    return eventExist;
  }

  }

