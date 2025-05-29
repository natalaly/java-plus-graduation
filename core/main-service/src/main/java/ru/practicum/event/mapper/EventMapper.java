package ru.practicum.event.mapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.event.dto.EventFullDto;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.enums.State;
import ru.practicum.event.model.Event;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mappers.UserMapper;

@UtilityClass
@Slf4j
public class EventMapper {

  public static Event toEvent(final NewEventDto eventDto, final UserDto initiator, final CategoryDto category) {
    log.debug("Mapping NewEventDto {} to the Event.", eventDto);
    Objects.requireNonNull(eventDto);
    Objects.requireNonNull(initiator);
    return new Event()
        .setAnnotation(eventDto.getAnnotation())
        .setCategory(CategoryMapper.toCategory(category))
        .setDescription(eventDto.getDescription())
        .setEventDate(eventDto.getEventDate())
        .setLocation(eventDto.getLocation())
        .setPaid(eventDto.getPaid())
        .setCreatedOn(LocalDateTime.now())
        .setInitiator(UserMapper.mapToUser(initiator))
        .setParticipantLimit(eventDto.getParticipantLimit())
        .setTitle(eventDto.getTitle())
        .setRequestModeration(eventDto.getRequestModeration())
        .setState(State.PENDING);
  }

  public static EventFullDto toFullDto(final Event event) {
    log.debug("Mapping Event {} to the EventFullDto.", event);
    Objects.requireNonNull(event);
    return new EventFullDto()
        .setId(event.getId())
        .setAnnotation(event.getAnnotation())
        .setCategory(CategoryMapper.toCategoryDto(event.getCategory()))
        .setConfirmedRequests(event.getConfirmedRequests())
        .setEventDate(event.getEventDate())
        .setInitiator(UserMapper.mapToUserShortDto(event.getInitiator()))
        .setPaid(event.getPaid())
        .setTitle(event.getTitle())
        .setViews(event.getViews())
        .setCreatedOn(event.getCreatedOn())
        .setDescription(event.getDescription())
        .setLocation(event.getLocation())
        .setParticipantLimit(event.getParticipantLimit())
        .setPublishedOn(event.getPublishedOn())
        .setRequestModeration(event.getRequestModeration())
        .setState(event.getState().name());
  }

  public static EventShortDto toShortDto(final Event event) {
    log.debug("Mapping Event {} to the EventShortDto.", event);
    Objects.requireNonNull(event);
    return new EventShortDto(
        event.getAnnotation(),
        CategoryMapper.toCategoryDto(event.getCategory()),
        event.getConfirmedRequests(),
        event.getEventDate(),
        event.getId(),
        UserMapper.mapToUserShortDto(event.getInitiator()),
        event.getPaid(),
        event.getTitle(),
        event.getViews());
  }

  public static List<EventShortDto> toShortDto(final List<Event> events) {
    if (events == null || events.isEmpty()) {
      return Collections.emptyList();
    }
    return events.stream()
        .map(EventMapper::toShortDto)
        .toList();
  }

  public static EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(
      final List<ParticipationRequest> confirmedRequests, final List<ParticipationRequest> rejectedRequests) {
    log.debug("Mapping parameters to the EventRequestStatusUpdateResult.");
    return new EventRequestStatusUpdateResult()
        .setConfirmedRequests(RequestMapper.mapToDto(confirmedRequests))
        .setRejectedRequests(RequestMapper.mapToDto(rejectedRequests));
  }
}
