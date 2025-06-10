package ru.practicum.request.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.RequestOperations;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestProcessingService;

@RestController
@RequestMapping("/internal/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class InternalRequestController implements RequestOperations {

  private final RequestProcessingService requestService;

  @Override
  @GetMapping("/events/{eventId}")
  public List<ParticipationRequestDto> getAllEventRequests(
      @PathVariable("eventId") @NotNull @Positive Long eventId) {
    log.info("Received request to get participation requests for event with ID {}.", eventId);
    final List<ParticipationRequestDto> requests = requestService.getByEventId(eventId);
    log.info("Returning {} participation requests.", requests.size());
    return requests;
  }

  @Override
  @PostMapping("/events/confirmed")
  public Map<Long, List<ParticipationRequestDto>> getConfirmedRequests(
      @RequestBody final List<Long> eventIds) {
    log.info("Received request to get confirmed requests for events with IDs {}.", eventIds);
    if (eventIds.isEmpty()) {
      log.info("No events IDs provided. Returning empty list.");
      return Map.of();
    }
    final Map<Long, List<ParticipationRequestDto>> requests = requestService.getConfirmedRequests(
        eventIds);
    log.info("Returning {} confirmed requests.", requests.size());
    return requests;
  }

  @Override
  @PostMapping
  public List<ParticipationRequestDto> updateRequests(
      @RequestBody final List<ParticipationRequestDto> requestsToUpdate) {
    log.info("Received request to update {} participation requests.",
        requestsToUpdate.size());
    if (requestsToUpdate.isEmpty()) {
      log.info("No requests provided. Returning empty list.");
      return List.of();
    }
    List<ParticipationRequestDto> requests = requestService.updateEventRequests(requestsToUpdate);
    log.info("Returning {} updated requests.", requests.size());
    return requests;
  }

  @Override
  @PostMapping("/events/{eventId}/pending")
  public List<ParticipationRequestDto> getEventPendingRequests(
      @RequestBody final List<Long> requestIds,
      @PathVariable("eventId") @NotNull @Positive Long eventId) {
    log.info("Received request to get pending requests for event with ID {}.", eventId);
    List<ParticipationRequestDto> requests = requestService.getEventPendingRequests(eventId,
        requestIds);
    log.info("Returning {} pending requests.", requests.size());
    return requests;
  }

}

