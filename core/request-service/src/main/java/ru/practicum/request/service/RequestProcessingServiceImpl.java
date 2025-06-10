package ru.practicum.request.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EventFullDto;
import ru.practicum.enums.StatusRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.client.event.EventClient;
import ru.practicum.request.client.user.UserClient;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestProcessingServiceImpl implements RequestProcessingService {

  private final RequestService requestService;
  private final UserClient userClient;
  private final EventClient eventClient;

  @Override
  public ParticipationRequestDto addRequest(final Long userId, final Long eventId) {
    validateUserExistsById(userId);
    final EventFullDto event = getEvent(eventId);
    ParticipationRequest requestSaved = requestService.addRequest(userId, event);
    return RequestMapper.mapToDto(requestSaved);
  }

  @Override
  public List<ParticipationRequestDto> getAll(final Long userId) {
    validateUserExistsById(userId);
    final List<ParticipationRequest> requests = requestService.getAll(userId);
    return RequestMapper.mapToDto(requests);
  }

  @Override
  public ParticipationRequestDto cancel(final Long userId, final Long requestId) {
    validateUserExistsById(userId);
    ParticipationRequest participationRequest = requestService.cancel(userId, requestId);
    return RequestMapper.mapToDto(participationRequest);
  }

  @Override
  public Map<Long, List<ParticipationRequestDto>> getConfirmedRequests(final List<Long> eventIds) {
    final List<ParticipationRequest> requests = requestService.getConfirmedRequestsByEventsIds(eventIds);
    return requests.stream()
        .map(RequestMapper::mapToDto)
        .collect(Collectors.groupingBy(ParticipationRequestDto::getEvent));
  }

  @Override
  public List<ParticipationRequestDto> getByEventId(final Long eventId) {
    final List<ParticipationRequest> requests = requestService.getByEventId(eventId);
    return requests.stream()
        .map(RequestMapper::mapToDto)
        .toList();
  }

  @Override
  public List<ParticipationRequestDto> updateEventRequests(final List<ParticipationRequestDto> requestsToUpdate) {
    List<ParticipationRequest> toUpdate = requestsToUpdate.stream()
        .map(RequestMapper::mapToEntity)
        .toList();
    List<ParticipationRequest> updatedRequests = requestService.updateEventRequests(toUpdate);
    return updatedRequests.stream()
        .map(RequestMapper::mapToDto)
        .toList();
  }

  @Override
  public List<ParticipationRequestDto> getEventPendingRequests(final Long eventId, final List<Long> requestIds) {
    final List<ParticipationRequest> requests =
        requestService.getEventRequestsByStatus(eventId, requestIds, StatusRequest.PENDING);
    return requests.stream()
        .map(RequestMapper::mapToDto)
        .toList();
  }

  private void validateUserExistsById(final Long userId) {
    log.debug("Checking if user id {} is not null and exists.", userId);
    if (userId == null || !userClient.existsById(userId)) {
      log.warn("Validation User with ID = {} is not null and exists in DB failed.", userId);
      throw new NotFoundException("User not found.");
    }
    log.debug("Success: user ID {} is not null and exists.", userId);
  }

  private EventFullDto getEvent(final Long eventId) {
    log.debug("Retrieving event with ID {} from event-service.", eventId);
    final EventFullDto event = eventClient.getEvent(eventId);
    log.debug("Event with ID {} found in event-service.", eventId);
    return event;
  }

}
