package ru.practicum.request.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventFullDto;
import ru.practicum.enums.State;
import ru.practicum.enums.StatusRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

  private final RequestRepository requestRepository;

  @Override
  @Transactional
  public ParticipationRequest addRequest(final Long userId, final EventFullDto event) {

    if (userId.equals(event.getInitiator().getId())) {
      throw new ConflictException("Нельзя добавить запрос на свое собственное событие");
    }

    if (!State.PUBLISHED.equals(State.valueOf(event.getState()))) {
      throw new ConflictException("Нельзя участвовать в неопубликованном событии");
    }

    ParticipationRequest existingRequest = requestRepository.findByRequesterIdAndEventId(userId,
        event.getId());
    if (existingRequest != null && !StatusRequest.CANCELED.equals(existingRequest.getStatus())) {
      throw new ConflictException("Вы уже отправили запрос на это событие");
    }

    int confirmedRequests = requestRepository.countAllByEventIdAndStatus(event.getId(),
        StatusRequest.CONFIRMED);

    ParticipationRequest participationRequest = new ParticipationRequest(userId, event.getId());

    if (event.getParticipantLimit() != 0 && event.getParticipantLimit() == confirmedRequests) {
      throw new ConflictException("Лимит запроса закончен");
    }

    if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
      participationRequest.setStatus(StatusRequest.CONFIRMED);
    }

    return requestRepository.save(participationRequest);
  }


  @Override
  @Transactional(readOnly = true)
  public List<ParticipationRequest> getAll(final Long userId) {
    return requestRepository.findAllByRequesterId(userId);
  }

  @Override
  @Transactional
  public ParticipationRequest cancel(final Long userId, final Long requestId) {

    ParticipationRequest participationRequest = requestRepository.findById(requestId)
        .orElseThrow(() -> new NotFoundException("Нету такого запроса"));

    if (!userId.equals(participationRequest.getRequesterId())) {
      throw new NotFoundException("Отменить может только владелец заявки");
    }

    participationRequest.setStatus(StatusRequest.CANCELED);

    return requestRepository.save(participationRequest);
  }

  @Override
  public List<ParticipationRequest> getConfirmedRequestsByEventsIds(final List<Long> eventIds) {
    log.debug("Fetching confirmed requests for events IDs {}.", eventIds);
    return requestRepository.findAllByEventIdInAndStatus(eventIds, StatusRequest.CONFIRMED);
  }

  @Override
  public List<ParticipationRequest> getByEventId(final Long eventId) {
    log.debug("Fetching requests for event ID {}.", eventId);
    return requestRepository.findAllByEventId(eventId);
  }

  @Override
  public List<ParticipationRequest> updateEventRequests(final List<ParticipationRequest> data) {
    log.debug("Updating {} requests statuses.", data.size());
    List<Long> requiredIds = data.stream().map(ParticipationRequest::getId).toList();
    List<ParticipationRequest> requests = requestRepository.findAllByIdIn(requiredIds);
    validateAllRequestsExists(requiredIds, requests);
    patchStatusField(requests, data);
    return requestRepository.saveAll(requests);
  }

  @Override
  public List<ParticipationRequest> getEventRequestsByStatus(final Long eventId,
                                                             final List<Long> requestIds,
                                                             final StatusRequest statusRequest) {
    log.debug("Fetching pending requests for event ID {}.", eventId);

    final List<ParticipationRequest> requests =
        requestRepository.findAllByIdInAndEventIdAndStatus(requestIds, eventId, statusRequest);
    return requests;
  }

  private void patchStatusField(final List<ParticipationRequest> target, final List<ParticipationRequest> source) {
    log.debug("Changing status field for {} requests.", source.size());
    Map<Long, StatusRequest> statusById = source.stream()
        .collect(
            Collectors.toMap(
                ParticipationRequest::getId,ParticipationRequest::getStatus));

    target.forEach(request ->
        request.setStatus(statusById.get(request.getId())));
  }

  private void validateAllRequestsExists(final List<Long> expectedId, final List<ParticipationRequest> actual) {
    log.debug("Validating all demanded requests exists in DB.");
    final Set<Long> actualIds = actual.stream()
        .map(ParticipationRequest::getId)
        .collect(Collectors.toSet());
    final Set<Long> notFoundRequests = expectedId.stream()
        .filter(id -> !actualIds.contains(id))
        .collect(Collectors.toSet());
    if (!notFoundRequests.isEmpty()) {
      throw new NotFoundException("Not found requests with IDs: " + notFoundRequests + " in DB.");
    }
  }
}
