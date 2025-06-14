package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.event.dto.EventRequestStatusUpdateRequest;
import ru.practicum.event.dto.EventRequestStatusUpdateResult;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.GetEventAdminRequest;
import ru.practicum.event.dto.GetEventPublicParam;
import ru.practicum.event.dto.NewEventDto;
import ru.practicum.event.dto.UpdateEventAdminRequest;
import ru.practicum.event.dto.UpdateEventUserRequest;

public interface EventProcessingService {

  /**
   * Saves a new event data initiated by a current user.
   */
  EventFullDto addEvent(Long initiatorId, NewEventDto eventDto);

  /**
   * Updates specified event with the provided data (Performed by ADMIN).
   */
  EventFullDto updateEvent(long eventId, UpdateEventAdminRequest param);

  /**
   * Updates the specified event by the current user (who is the initiator of the event).
   */
  EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto);

  /**
   * Retrieves complete data about specific event created by a current user.
   */
  EventFullDto getEvent(Long initiatorId, Long eventId);

  /**
   * Retrieves detailed information about an event by its ID.
   */
  EventFullDto getEvent(Long eventId);

  /**
   * Retrieves detailed information about a published event by its ID.
   */
  EventFullDto getPublishedEvent(Long eventId);

  /**
   * Retrieves all existed in DB events (performed by ADMIN).
   */
  List<EventFullDto> getEvents(GetEventAdminRequest param);

  /**
   * Retrieving published events with filtering options.
   */
  List<EventShortDto> getEvents(GetEventPublicParam param, HttpServletRequest request);

  /**
   * Retries all events created by current user.
   */
  List<EventShortDto> getEvents(Long initiatorId, Integer from, Integer size);

  /**
   * Retrieves information about participation requests for the current user's event.
   */
  List<ParticipationRequestDto> getRequests(Long initiatorId, Long eventId);

  /**
   * Updates the participation request statuses for the specified event of the current user. The
   * statuses can be changed to either {@code CONFIRMED} or {@code REJECTED}.
   */
  EventRequestStatusUpdateResult updateRequestsStatus(Long userId,
                                                      Long eventId,
                                                      EventRequestStatusUpdateRequest updateStatusDto);

  /**
   * Provides information wherever the vent with given ID exists in the system.
   */
  boolean eventExists(Long eventId);
}
