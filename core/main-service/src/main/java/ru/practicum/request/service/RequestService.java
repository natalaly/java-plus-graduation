package ru.practicum.request.service;

import java.util.List;
import ru.practicum.event.model.Event;
import ru.practicum.request.model.ParticipationRequest;

public interface RequestService {

    ParticipationRequest addRequest(Long userId, Event event);

    List<ParticipationRequest> getAll(Long userId);

    ParticipationRequest cancel(Long userId, Long requestId);

}
