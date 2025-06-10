package ru.practicum.request.controller;

import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.request.service.RequestProcessingService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
public class PrivateRequestController {

  private final RequestProcessingService requestService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  private ParticipationRequestDto addRequest(@PathVariable @NonNull Long userId,
                                             @RequestParam @NonNull Long eventId) {
    log.debug("Received request from user {} to add a new participation request for event ID {}.",
        userId, eventId);
    ParticipationRequestDto request = requestService.addRequest(userId, eventId);
    log.debug("Participation request for event ID {} saved with ID {}.",
        eventId, request.getId());
    return request;
  }

  @GetMapping
  private List<ParticipationRequestDto> getAllRequest(@PathVariable @NonNull Long userId) {
    log.debug("Received request from user {} to get all participation requests.", userId);
    List<ParticipationRequestDto> requests = requestService.getAll(userId);
    log.debug("Returning {} requests.", requests.size());
    return requests;
  }

  @PatchMapping("/{requestId}/cancel")
  private ParticipationRequestDto cancel(@PathVariable @NonNull Long userId,
                                         @PathVariable @NonNull Long requestId) {
    log.debug("Received request from user {} to cancel their participation request {}.", userId,
        requestId);
    ParticipationRequestDto request = requestService.cancel(userId, requestId);
    log.debug("Participation request{} is canceled now.", requestId);
    return request;
  }
}

