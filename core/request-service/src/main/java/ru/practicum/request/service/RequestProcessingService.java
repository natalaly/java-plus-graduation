package ru.practicum.request.service;

import java.util.List;
import java.util.Map;
import ru.practicum.dto.ParticipationRequestDto;

public interface RequestProcessingService {

  ParticipationRequestDto addRequest(Long userId, Long eventId);

  List<ParticipationRequestDto> getAll(Long requesterId);

  ParticipationRequestDto cancel(Long userId, Long requestId);

  Map<Long, List<ParticipationRequestDto>> getConfirmedRequests(List<Long> eventIds);

  List<ParticipationRequestDto> getByEventId(Long eventId);

  List<ParticipationRequestDto> updateEventRequests(List<ParticipationRequestDto> requestsToUpdate);

  List<ParticipationRequestDto> getEventPendingRequests(Long eventId, List<Long> requestIds);
}
