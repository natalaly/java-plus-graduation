package ru.practicum.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.ParticipationRequestDto;

public interface RequestOperations {

  @GetMapping("/events/{eventId}")
  List<ParticipationRequestDto> getAllEventRequests(@PathVariable("eventId") @NotNull @Positive Long eventId);

  @PostMapping("/events/confirmed")
  Map<Long, List<ParticipationRequestDto>> getConfirmedRequests(@RequestBody final List<Long> eventIds);

  @PostMapping
  List<ParticipationRequestDto> updateRequests(@RequestBody final List<ParticipationRequestDto> requestsToUpdate);

  @PostMapping("/events/{eventId}/pending")
  List<ParticipationRequestDto> getEventPendingRequests(@RequestBody final List<Long> requestIds,
                                                        @PathVariable("eventId") @NotNull @Positive Long eventId);

}
