package ru.practicum.request.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.client.UserClient;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.dto.ParticipationRequestDto;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestProcessingServiceImpl implements RequestProcessingService {

  private final RequestService requestService;
  private final UserClient userClient;
  private final EventRepository eventClient;

  @Override
  public ParticipationRequestDto addRequest(final Long userId, final Long eventId) {
    validateUserExistsById(userId);
    final Event event = getEvent(eventId);// TODO EventFullDto
    ParticipationRequest requestSaved = requestService.addRequest(userId,event);
    return RequestMapper.mapToDto(requestSaved);
  }

  @Override
  public List<ParticipationRequestDto> getAll(final Long userId) {
    validateUserExistsById(userId);
    final List<ParticipationRequest> requests = requestService.getAll(userId);
    return RequestMapper.mapToDto(requests);
  }

  @Override
  public ParticipationRequestDto cancel(Long userId, Long requestId) {
    validateUserExistsById(userId);
    ParticipationRequest participationRequest = requestService.cancel(userId,requestId);
    return RequestMapper.mapToDto(participationRequest);
  }

  private void validateUserExistsById(final Long userId) {
    log.debug("Validating user id {} is not null and exists.", userId);
    if (userId == null || !userClient.existsById(userId)) {
      log.warn("Validation User with ID = {} is not null and exists in DB failed.", userId);
      throw new NotFoundException("User not found.");
    }
    log.debug("Success: user ID {} is not null and exists.", userId);
  }

  private Event getEvent(final Long eventId) {
    log.debug("Getting event with ID {} if exists.", eventId);
    return eventClient.findById(eventId)
        .orElseThrow(() -> new NotFoundException("Нету такого event"));
  }

}
