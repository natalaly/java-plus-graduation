package ru.practicum.request.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.enums.StatusRequest;
import ru.practicum.request.model.ParticipationRequest;

@UtilityClass
@Slf4j
public class RequestMapper {

  public ParticipationRequestDto mapToDto(final ParticipationRequest participationRequest) {
    log.debug("Mapping participationRequest {} to RequestDto.", participationRequest);
    Objects.requireNonNull(participationRequest);
    return new ParticipationRequestDto()
        .setId(participationRequest.getId())
        .setRequester(participationRequest.getRequesterId())
        .setEvent(participationRequest.getEventId())
        .setCreated(participationRequest.getCreated())
        .setStatus(participationRequest.getStatus().name());
  }

  public List<ParticipationRequestDto> mapToDto(final List<ParticipationRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      return Collections.emptyList();
    }
    return requests.stream()
        .map(RequestMapper::mapToDto)
        .toList();
  }

  public ParticipationRequest mapToEntity(final ParticipationRequestDto dto) {
    log.debug("Mapping participationRequestDto {} to ParticipationRequest.", dto);
    Objects.requireNonNull(dto);
    return new ParticipationRequest()
        .setId(dto.getId())
        .setRequesterId(dto.getRequester())
        .setEventId(dto.getEvent())
        .setCreated(dto.getCreated())
        .setStatus(StatusRequest.valueOf(dto.getStatus()));
  }

}
