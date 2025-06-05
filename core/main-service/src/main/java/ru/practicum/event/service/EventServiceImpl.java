package ru.practicum.event.service;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.model.Category;
import ru.practicum.category.service.CategoryService;
import ru.practicum.event.dto.GetEventAdminRequest;
import ru.practicum.event.dto.GetEventPublicParam;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.enums.State;
import ru.practicum.event.enums.StateAction;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

  private final EventRepository eventRepository;
  private final CategoryService categoryService;

  /**
   * Saves a new event data initiated by a current user.
   */
  @Override
  public Event addEvent(final Event newEvent, final Long categoryId) {
    log.debug("Persisting a new event: {} posted by user with ID={}.", newEvent,
        newEvent.getInitiatorId());
    final Category category = fetchCategory(categoryId);
    final Event eventToSave = newEvent.setCategory(category);
    return eventRepository.save(eventToSave);
  }


  /**
   * Updates specified event with the provided data (Performed by ADMIN).
   */
  @Override
  public Event updateEvent(final long eventId, final UpdateEventAdminRequest param) {
    log.debug("Updating event, ID: {}, with data: {}. Performed by Admin.", eventId, param);
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found."));

    validateEventUpdatable(event,param);
    patchEventFields(event, param);

    return eventRepository.save(event);
  }

  /**
   * Updates the specified event by the current user (who is the initiator of the event).
   */
  @Override
  public Event updateEvent(final Long userId, final Long eventId, final UpdateEventUserRequest eventDto) {
    log.debug("Updating event ID:{}, by user - initiator, ID: {} with data: {}.",
        eventId, userId, eventDto);

    final Event eventToUpdate = fetchEvent(eventId, userId);

    validateEventUpdatable(eventToUpdate, null);
    patchEventFields(eventToUpdate, eventDto);

    return eventRepository.save(eventToUpdate);
  }

  /**
   * Retrieves complete data about specific event created by current user.
   */
  @Transactional(readOnly = true)
  @Override
  public Event getEvent(final Long initiatorId, final Long eventId) {
    log.debug("Fetching event ID={}, posted by user with ID={}.", eventId, initiatorId);
    return fetchEvent(eventId, initiatorId);
  }

  /**
   * Retrieves detailed information about a published event by its ID.
   */
  @Transactional(readOnly = true)
  @Override
  public Event getEvent(final Long eventId) {
    log.debug("Fetching published event ID={}.", eventId);
    Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
        .orElseThrow(() -> new NotFoundException(
            "Event with id " + eventId + " not found or not published"));
    return event;
  }

  /**
   * Retrieves all existed in DB events that match the given conditions in the GetEventAdminRequest(performed by ADMIN).
   */
  @Transactional(readOnly = true)
  @Override
  public List<Event> getEvents(GetEventAdminRequest param) {
    log.debug("Retrieving all events from DB with conditions: {} for Admin to use.", param);
    List<Event> events = eventRepository.adminFindEvents(
        param.getUsers(),
        param.getStates(),
        param.getCategories(),
        param.getRangeStart(),
        param.getRangeEnd(),
        param.getFrom(),
        param.getSize()
    );
    log.debug("Found {} events in DB.", events.size());
    return events;
  }

  /**
   * Retries all events created by current user.
   */
  @Transactional(readOnly = true)
  @Override
  public List<Event> getEvents(final Long initiatorId, final Integer from,
                                       final Integer size) {
    log.debug("Fetching events posted by user with ID={}.", initiatorId);
    final PageRequest page = PageRequest.of(from / size, size);
    return eventRepository.findAllByInitiatorId(initiatorId, page).getContent();
  }

  /**
   * Retrieving published events with filtering options.
   */
  @Transactional(readOnly = true)
  @Override
  public List<Event> getEvents(final GetEventPublicParam param, final HttpServletRequest request) {
    log.debug("Fetching published events with params {}", param);
    if (param.getRangeStart() != null && param.getRangeEnd() != null &&
        param.getRangeStart().isAfter(param.getRangeEnd())) {
      throw new BadRequestException("Start date should be before end date");
    }
    return eventRepository.publicGetPublishedEvents(
        param.getText(),
        param.getCategories(),
        param.getPaid(),
        param.getRangeStart(),
        param.getRangeEnd(),
        param.getOnlyAvailable(),
        param.getSort(),
        param.getFrom(),
        param.getSize());
  }

  /**
   *  Retrieves a set of events based on the provided event IDs.
   */
  @Transactional(readOnly = true)
  @Override
  public Set<Event> getEvents(final Set<Long> eventIds) {
    log.debug("Retrieving set of events by theirs IDs: {}.", eventIds);
    Objects.requireNonNull(eventIds);
    return eventIds.isEmpty() ? Set.of() : eventRepository.findAllDistinctByIdIn(eventIds);
  }


  private Event fetchEvent(final Long eventId, final Long initiatorId) {
    log.debug("Fetching event with ID {} and initiator ID {}.", eventId, initiatorId);
    return eventRepository.findByIdAndInitiatorId(eventId, initiatorId)
        .map(result -> result.getEvent().setConfirmedRequests(result.getConfirmedRequests()))
        .orElseThrow(
            () -> {
              log.warn("Event ID={} with initiator ID={} not found.", eventId, initiatorId);
              return new NotFoundException("Event was not found.");
            });
  }

  private void patchEventFields(final Event event, final UpdateEventAdminRequest param) {
    log.debug("Applying the patch on Event fields by Admin.");
    patchAllButStateActionField(event,
        param.getAnnotation(),
        param.getDescription(),
        param.getEventDate(),
        param.getLocation(),
        param.getPaid(),
        param.getParticipantLimit(),
        param.getRequestModeration(),
        param.getTitle(),
        param.getCategory()
    );
    applyPatchStateAction(event, param.getStateAction(),true);
  }


  private void patchEventFields(final Event target, final UpdateEventUserRequest dataSource) {
    log.debug("Applying the patch on Event fields bu user.");
    patchAllButStateActionField(target,
        dataSource.getAnnotation(),
        dataSource.getDescription(),
        dataSource.getEventDate(),
        dataSource.getLocation(),
        dataSource.getPaid(),
        dataSource.getParticipantLimit(),
        dataSource.getRequestModeration(),
        dataSource.getTitle(),
        dataSource.getCategory()
    );
    applyPatchStateAction(target, dataSource.getStateAction(),false);
  }

  private void patchAllButStateActionField(final Event event,
                                           final String annotation,
                                           final String description,
                                           final LocalDateTime eventDate,
                                           final Location location,
                                           final Boolean paid,
                                           final Integer participantLimit,
                                           final Boolean requestModeration,
                                           final String title,
                                           final Long categoryId) {

    Optional.ofNullable(annotation).ifPresent(event::setAnnotation);
    Optional.ofNullable(description).ifPresent(event::setDescription);
    Optional.ofNullable(eventDate).ifPresent(event::setEventDate);
    Optional.ofNullable(location).ifPresent(event::setLocation);
    Optional.ofNullable(paid).ifPresent(event::setPaid);
    Optional.ofNullable(participantLimit).ifPresent(event::setParticipantLimit);
    Optional.ofNullable(requestModeration).ifPresent(event::setRequestModeration);
    Optional.ofNullable(title).ifPresent(event::setTitle);
    Optional.ofNullable(categoryId)
        .map(this::fetchCategory)
        .ifPresent(event::setCategory);
  }

  private void applyPatchStateAction(final Event event, final String action, final boolean isAdmin) {
    Optional.ofNullable(action)
        .map(StateAction::fromString)
        .map(StateAction::getState)
        .ifPresent(newState -> {
          event.setState(newState);

          if (isAdmin && State.PUBLISHED.equals(newState)) {
            event.setPublishedOn(LocalDateTime.now());
          }
        });
  }

  private void validateEventUpdatable(final Event event, final @Nullable UpdateEventAdminRequest param) {
    log.debug("Validate event date is in the future and has right state.");
    if (param != null) {
      validateEventDate(event.getEventDate(), 1);
      validateEventState(event,param);
    } else {
      validateEventDate(event.getEventDate(), 2);
      validateEventState(event);
    }
  }

  private void validateEventState(final Event event) {
    if (State.PUBLISHED.equals(event.getState())) {
      throw new ConflictException("Only pending or canceled events can be changed");
    }
    log.debug("Event state is valid (pending or canceled): {}.", event.getState());
  }

  private void validateEventState(final Event event, final UpdateEventAdminRequest param) {
    if (param.getStateAction() != null) {
      if (param.getStateAction().equals(StateAction.PUBLISH_EVENT.name()) && !event.getState()
          .equals(State.PENDING)) {
        throw new ConflictException("Cannot publish the event because it's not in the right state: "
            + event.getState());
      }
      if (param.getStateAction().equals(StateAction.REJECT_EVENT.name()) && event.getState()
          .equals(State.PUBLISHED)) {
        throw new ConflictException("Cannot reject the event because it is already published.");
      }
    }
  }

  private void validateEventDate(final LocalDateTime eventDate, final int minimumTimeGap) {
    if (!eventDate.isAfter(LocalDateTime.now().plusHours(minimumTimeGap))) {
      throw new ConflictException("The event date must be at least two hours in the future.");
    }
    log.debug("Event date is valid (at least is two hours ahead fom now): {}", eventDate);
  }

  private Category fetchCategory(final Long categoryId) {
    log.debug("Fetching category with ID={}", categoryId);
    return CategoryMapper.toCategory(categoryService.getCategoryById(categoryId));
  }

}
