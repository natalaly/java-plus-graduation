package ru.practicum.request.service;

import java.util.List;
import ru.practicum.dto.EventFullDto;
import ru.practicum.enums.StatusRequest;
import ru.practicum.request.model.ParticipationRequest;

public interface RequestService {

  ParticipationRequest addRequest(Long userId, EventFullDto event);

  List<ParticipationRequest> getAll(Long userId);

  ParticipationRequest cancel(Long userId, Long requestId);

  List<ParticipationRequest> getConfirmedRequestsByEventsIds(List<Long> eventIds);

  List<ParticipationRequest> getByEventId(Long eventId);

  List<ParticipationRequest> updateEventRequests(List<ParticipationRequest> requests);

  List<ParticipationRequest> getEventRequestsByStatus(Long eventId, List<Long> requestIds, StatusRequest statusRequest);
}
