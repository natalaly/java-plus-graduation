package ru.practicum.request.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.enums.State;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.StatusRequest;
import ru.practicum.request.repository.RequestRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

  private final RequestRepository requestRepository;

  @Override
  @Transactional
  public ParticipationRequest addRequest(final Long userId, final Event event) {

    if (userId.equals(event.getInitiatorId())) {
      throw new ConflictException("Нельзя добавить запрос на свое собственное событие");
    }

    if (!event.getState().equals(State.PUBLISHED)) {
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
}
