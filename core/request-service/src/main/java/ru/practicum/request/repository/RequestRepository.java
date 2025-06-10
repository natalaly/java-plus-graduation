package ru.practicum.request.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.enums.StatusRequest;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

  List<ParticipationRequest> findAllByRequesterId(Long userId);

  List<ParticipationRequest> findAllByEventId(Long eventId);

  int countAllByEventIdAndStatus(Long eventId, StatusRequest status);

  ParticipationRequest findByRequesterIdAndEventId(Long userId, Long eventId);

  List<ParticipationRequest> findAllByEventIdInAndStatus(List<Long> eventIds,
                                                         StatusRequest statusRequest);

  List<ParticipationRequest> findAllByIdIn(List<Long> requestsIds);

  List<ParticipationRequest> findAllByIdInAndEventIdAndStatus(List<Long> requestIds,
                                                              Long eventId,
                                                              StatusRequest statusRequest);
}