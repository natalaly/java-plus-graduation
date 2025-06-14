package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.client.request.RequestClient;
import ru.practicum.client.user.UserClient;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.enums.State;
import ru.practicum.enums.StatusRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.GetEventAdminRequest;
import ru.practicum.event.dto.GetEventPublicParam;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventProcessingServiceImpl implements EventProcessingService {

  private final EventService eventService;

  private final UserClient userClient;
  private final RequestClient requestClient;
  private final StatsClient statsClient;

  /**
   * Saves a new event data initiated by a current user.
   *
   * @param initiatorId
   * @param eventDto
   */
  @Override
  public EventFullDto addEvent(final Long initiatorId, final NewEventDto eventDto) {
    log.debug("Adding new event: {} posted by user with ID {}.", eventDto, initiatorId);
    final UserShortDto initiator = getUser(initiatorId);
    final Event eventToSave = EventMapper.toEvent(eventDto, initiatorId);
    final Event saved = eventService.addEvent(eventToSave, eventDto.getCategory());

    return EventMapper.toFullDto(saved.setInitiator(initiator));
  }

  /**
   * Updates specified event with the provided data (Performed by ADMIN).
   *
   * @param eventId
   * @param param
   */
  @Override
  public EventFullDto updateEvent(final long eventId, final UpdateEventAdminRequest param) {
    log.debug("Updating event, ID:{} with data:{}. Performed by ADMIN.", eventId, param);

    final Event eventUpdated = eventService.updateEvent(eventId, param);
    final UserShortDto initiator = getUser(eventUpdated.getInitiatorId());

    eventUpdated.setInitiator(initiator);
    setViews(List.of(eventUpdated));
    setConfirmedRequests(List.of(eventUpdated));

    return EventMapper.toFullDto(eventUpdated);
  }

  /**
   * Updates the specified event by the current user (who is the initiator of the event).
   *
   * @param userId
   * @param eventId
   * @param eventDto
   */
  @Override
  public EventFullDto updateEvent(final Long userId, final Long eventId,
                                  final UpdateEventUserRequest eventDto) {
    log.debug("Updating event, ID:{} with data:{}. Performed by Initiator {}.", eventId, eventDto,
        userId);

    final Event eventUpdated = eventService.updateEvent(userId, eventId, eventDto);
    final UserShortDto initiator = getUser(eventUpdated.getInitiatorId());

    eventUpdated.setInitiator(initiator);
    setViews(List.of(eventUpdated));
    setConfirmedRequests(List.of(eventUpdated));

    return EventMapper.toFullDto(eventUpdated);
  }

  /**
   * Retrieves complete data about a specific event created by a current user.
   *
   * @param initiatorId
   * @param eventId
   */
  @Override
  public EventFullDto getEvent(final Long initiatorId, final Long eventId) {
    log.debug("Getting event with ID={}, posted by user with ID={}.", eventId, initiatorId);
    final Event event = eventService.getEvent(initiatorId, eventId);
    event.setInitiator(getUser(initiatorId));
    setViews(List.of(event));
    setConfirmedRequests(List.of(event));

    return EventMapper.toFullDto(event);
  }

  /**
   * Retrieves detailed information about an event by its ID with any state.
   *
   * @param eventId
   */
  @Override
  public EventFullDto getEvent(final Long eventId) {
    log.debug("Getting event with ID={}.", eventId);

    final Event event = eventService.getEvent(eventId);

    event.setInitiator(getUser(event.getInitiatorId()));
    setViews(List.of(event));
    setConfirmedRequests(List.of(event));

    return EventMapper.toFullDto(event);
  }

  /**
   * Retrieves detailed information about a published event by its ID.
   *
   * @param eventId
   */
  @Override
  public EventFullDto getPublishedEvent(final Long eventId) {
    log.debug("Getting published event with ID={}.", eventId);

    final Event event = eventService.getEvent(eventId, State.PUBLISHED);

    event.setInitiator(getUser(event.getInitiatorId()));
    setViews(List.of(event));
    setConfirmedRequests(List.of(event));

    return EventMapper.toFullDto(event);
  }

  /**
   * Retrieves all existed events (performed by ADMIN).
   *
   * @param param
   */
  @Override
  public List<EventFullDto> getEvents(final GetEventAdminRequest param) {
    log.debug("Getting all events with param: {}.Performed by Admin.", param);
    final List<Event> events = eventService.getEvents(param);
    setInitiators(events);
    setViews(events);
    setConfirmedRequests(events);
    return events.stream()
        .map(EventMapper::toFullDto)
        .collect(Collectors.toList());
  }

  /**
   * Retries all events created by the current user.
   *
   * @param initiatorId
   * @param from
   * @param size
   */
  @Override
  public List<EventShortDto> getEvents(final Long initiatorId, final Integer from,
                                       final Integer size) {
    log.debug("Getting all events of user: {}.", initiatorId);
    validateUserExist(initiatorId);
    final UserShortDto initiator = getUser(initiatorId);
    final List<Event> events = eventService.getEvents(initiatorId, from, size);
    setInitiators(events);
    setConfirmedRequests(events);
    setViews(events);
    return EventMapper.toShortDto(events);
  }

  /**
   * Retrieving published events with filtering options.
   *
   * @param param
   * @param request
   */
  @Override
  public List<EventShortDto> getEvents(GetEventPublicParam param, HttpServletRequest request) {
    log.debug("Retrieving published events satisfied parameters: {}.", param);
    final List<Event> events = eventService.getEvents(param, request);
    setInitiators(events);
    setViews(events);
    setConfirmedRequests(events);
    return events.stream()
        .map(EventMapper::toShortDto)
        .toList();
  }

  /**
   * Retrieves information about participation requests for the current user's event.
   *
   * @param initiatorId
   * @param eventId
   */
  @Override
  public List<ParticipationRequestDto> getRequests(final Long initiatorId, final Long eventId) {
    log.debug("Retrieving event participants for the event ID {}, initiator {}.  ", eventId,
        initiatorId);
    final Event event = eventService.getEvent(initiatorId, eventId);

    return getRequestsByEventId(event.getId());
  }

  /**
   * Updates the participation request statuses for the specified event of the current user. The
   * statuses can be changed to either {@code CONFIRMED} or {@code REJECTED}.
   *
   * @param userId
   * @param eventId
   * @param updateStatusDto
   */
  @Override
  public EventRequestStatusUpdateResult updateRequestsStatus(final Long userId,
                                                             final Long eventId,
                                                             final EventRequestStatusUpdateRequest updateStatusDto) {
    log.debug("Updating statuses for the event participation requests {}  "
            + "for the event ID {}, performed by current user/initiator {}.",
        updateStatusDto, eventId, userId);

    validateUserExist(userId);
    final Event event = eventService.getEvent(userId, eventId);
    setConfirmedRequests(List.of(event));
    final StatusRequest newStatus = updateStatusDto.getStatus();
    final Boolean isModerated = event.getRequestModeration();
    final Integer participantLimit = event.getParticipantLimit();
    final Integer confirmed = event.getConfirmedRequests();

    if (!isModerated || participantLimit == 0) {
      log.debug("Event is not moderated or participant limit is 0. ");
      return autoConfirmRequests(updateStatusDto.getRequestIds(), eventId);
    }

    final List<ParticipationRequestDto> requestsToUpdate = getPendingRequests(
        updateStatusDto.getRequestIds(), eventId);

    int availableSlots = participantLimit - confirmed;
    if (availableSlots <= 0) {
      log.warn(
          "Participant limit for the event {} has been reached: limit={}, confirmed requests={}.",
          eventId, event.getParticipantLimit(), event.getConfirmedRequests());
      throw new ConflictException("Participant limit for this event has been reached.");
    }

    return processRequestsWithLimit(requestsToUpdate, newStatus, availableSlots);
  }

  /**
   * Provides information wherever the vent with given ID exists in the system.
   *
   * @param eventId
   */
  @Override
  public boolean eventExists(final Long eventId) {
    log.debug("Checking if event with ID={} exists.", eventId);
    return eventService.eventExistsById(eventId);
  }

  private void setInitiators(final Collection<Event> events) {
    log.debug("Setting initiators for events: {}", events);
    final List<Long> initiatorIds = events.stream()
        .map(Event::getInitiatorId)
        .toList();
    final Map<Long, UserShortDto> initiators = getUsersByIdIn(initiatorIds).stream()
        .collect(Collectors.toMap(UserShortDto::getId, Function.identity()));

    events.forEach(event -> event.setInitiator(initiators.get(event.getInitiatorId())));
  }

  private List<UserShortDto> getUsersByIdIn(final List<Long> initiatorIds) {
    log.debug("Getting users with IDs: {} from user-service.", initiatorIds);
    List<UserShortDto> users = userClient.getUsers(initiatorIds);
    log.debug("Successfully git users with IDs: {} from user-service.", initiatorIds);
    return users;
  }

  private UserShortDto getUser(final Long userId) {
    log.debug("Fetching user with ID={} from user-service.", userId);
    UserShortDto user = userClient.getUser(userId);
    log.debug("Successfully fetched user: {} from user-service.", user);
    return user;
  }

  private void validateUserExist(final Long userId) {
    log.debug("Validating user ID={} is not null and exists.", userId);
    if (userId == null || !userClient.existsById(userId)) {
      log.warn("Validation User with ID={} is not null and exists in DB failed.", userId);
      throw new NotFoundException("User not found.");
    }
    log.debug("Success: user ID={} is not null and exists.", userId);
  }

  private void setConfirmedRequests(final List<Event> events) {
    log.debug("Setting Confirmed requests to the events list, size {}.", events.size());
    if (events.isEmpty()) {
      log.debug("Events list is empty.");
      return;
    }
    final List<Long> eventIds = events.stream().map(Event::getId).toList();

    final Map<Long, List<ParticipationRequestDto>> confirmedRequests = getConfirmedRequests(eventIds);

    events.forEach(event ->
        event.setConfirmedRequests(
            confirmedRequests.getOrDefault(event.getId(), List.of()).size()));
    log.debug("Confirmed requests has set successfully to the events with IDs {}.", eventIds);
  }

  private Map<Long, List<ParticipationRequestDto>> getConfirmedRequests(final List<Long> eventIds) {
    log.debug("Sending request to get confirmed requests for the events with IDs {}.", eventIds);
    final Map<Long, List<ParticipationRequestDto>> confirmedRequests =
        requestClient.getConfirmedRequests(eventIds);
    log.debug("Successfully retrieved {} confirmed requests.", confirmedRequests.size());
    return confirmedRequests;
  }

  private List<ParticipationRequestDto> getRequestsByEventId(final Long eventId) {
    log.debug("Sending request to get participation requests for the event ID {} .", eventId);
    List<ParticipationRequestDto> requests = requestClient.getAllEventRequests(eventId);
    log.debug("Successfully retrieved requests for the event ID {} .", eventId);
    return requests;
  }

  private EventRequestStatusUpdateResult autoConfirmRequests(final List<Long> requestIds,
                                                             final Long eventId) {
    log.debug("Automatically confirming All requests for event ID {}.", eventId);
    final List<ParticipationRequestDto> requestsToUpdate = getPendingRequests(requestIds, eventId);
    requestsToUpdate.forEach(request -> request.setStatus(StatusRequest.CONFIRMED.name()));

    final List<ParticipationRequestDto> confirmedRequests = updateRequests(requestsToUpdate);
    return EventMapper.toEventRequestStatusUpdateResult(confirmedRequests, Collections.emptyList());
  }

  private List<ParticipationRequestDto> updateRequests(
      final List<ParticipationRequestDto> requestsToUpdate) {
    log.debug("Sending request to update {} requests to status CONFIRMED.", requestsToUpdate.size());
    final List<ParticipationRequestDto> updated = requestClient.updateRequests(requestsToUpdate);
    log.debug("Successfully updated {} requests to status CONFIRMED.", updated.size());
    return updated;
  }

  private List<ParticipationRequestDto> getPendingRequests(final List<Long> requestIds,
                                                           final Long eventId) {
    log.debug("Sending request to get participation requests with IDs:{} and PENDING status.",
        requestIds);
    final List<ParticipationRequestDto> requests = requestClient.getEventPendingRequests(requestIds,
        eventId);
    log.debug("Successfully retrieved {} requests with IDs : {}.", requests.size(), requestIds);

    if (requestIds.size() > requests.size()) {
      log.warn("StatusRequest should be PENDING for all requests to be updated.");
      throw new ConflictException(
          "StatusRequest should be PENDING for all requests to be updated.");
    }
    return requests;
  }

  private EventRequestStatusUpdateResult processRequestsWithLimit(
      final List<ParticipationRequestDto> requestsToUpdate,
      final StatusRequest newStatus, final Integer availableSlots) {

    log.debug("Processing requests {} to update status with {}. Available slots ={}",
        requestsToUpdate, newStatus, availableSlots);

    final List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
    final List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
    int available = availableSlots;

    if (newStatus.equals(StatusRequest.REJECTED)) {
      requestsToUpdate.forEach(r -> r.setStatus(newStatus.name()));
      rejectedRequests.addAll(updateRequests(requestsToUpdate));
    } else {
      for (ParticipationRequestDto request : requestsToUpdate) {
        if (available > 0) {
          request.setStatus(StatusRequest.CONFIRMED.name());
          confirmedRequests.add(request);
          available--;
        } else {
          request.setStatus(StatusRequest.REJECTED.name());
          rejectedRequests.add(request);
        }
      }
    }
    updateRequests(confirmedRequests);
    updateRequests(rejectedRequests);
    return EventMapper.toEventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
  }

  private void setViews(final List<Event> events) {
    log.debug("Setting views to the events list.");

    if (events == null || events.isEmpty()) {
      log.debug("Events list is empty.");
      return;
    }

    final LocalDateTime start = events.stream()
        .min(Comparator.comparing(Event::getCreatedOn))
        .map(Event::getCreatedOn)
        .orElse(LocalDateTime.now());

    final LocalDateTime end = LocalDateTime.now();

    final List<String> uris = events.stream()
        .map(e -> buildEventUri(e.getId()))
        .toList();

    log.debug("Calling StatsClient with parameters: start={}, end={}, uris={}, unique={}.",
        start, end, uris, true);
    final List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true).getBody();
    log.debug("Successfully retrieved views {}.", stats);

    final Map<String, Long> views = stats == null
        ? Collections.emptyMap()
        : stats.stream()
            .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));

    events.forEach(event ->
        event.setViews(views.getOrDefault(buildEventUri(event.getId()), 0L)));
    log.debug("Views has set successfully.");

  }

  private String buildEventUri(final Long eventId) {
    return String.format("/events/%d", eventId);
  }

}
