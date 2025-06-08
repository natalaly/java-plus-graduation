package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;
import ru.practicum.enums.State;
import ru.practicum.event.dto.GetEventAdminRequest;
import ru.practicum.event.dto.GetEventPublicParam;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;
import ru.practicum.event.model.Event;

public interface EventService {

  /**
   * Saves a new event data initiated by a current user.
   */
  Event addEvent(Event newEvent, Long categoryId);

  /**
   * Updates specified event with the provided data (Performed by ADMIN).
   */
  Event updateEvent(long eventId, UpdateEventAdminRequest param);

  /**
   * Updates the specified event by the current user (who is the initiator of the event).
   */
  Event updateEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto);

  /**
   * Retrieves complete data about specific event created by a current user.
   */
  Event getEvent(Long initiatorId, Long eventId);

  /**
   * Retrieves detailed information about an event with certain Status and ID.
   */
  Event getEvent(Long eventId, State state);

  /**
   * Retrieves detailed information about an event by its ID.
   */
  Event getEvent(Long eventId);

  /**
   * Retrieves all existed in DB events (performed by ADMIN).
   */
  List<Event> getEvents(GetEventAdminRequest param);

  /**
   * Retries all events created by current user.
   */
  List<Event> getEvents(Long initiatorId, Integer from, Integer size);

  /**
   * Retrieving published events with filtering options.
   */
  List<Event> getEvents(GetEventPublicParam param, HttpServletRequest request);

  /**
   *  Retrieves a set of events based on the provided event IDs.
   */
  Set<Event> getEvents(Set<Long> events);

  /**
   * Checks whether the event exists in the DB.
   */
  boolean eventExistsById(Long eventId);

}
