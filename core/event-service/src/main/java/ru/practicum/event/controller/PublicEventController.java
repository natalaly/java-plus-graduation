package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EventFullDto;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.dto.GetEventPublicParam;
import ru.practicum.event.enums.SortType;
import ru.practicum.event.service.EventProcessingService;

@RestController
@RequestMapping("/events")
@Slf4j
@RequiredArgsConstructor
public class PublicEventController {

  private static final String USER_ID_HEADER = "X-EWM-USER-ID";

  private final EventProcessingService eventService;

  @GetMapping
  public List<EventShortDto> getEvents(@RequestParam(value = "text", required = false) String text,
                                       @RequestParam(value = "categories", required = false) List<Long> categories,
                                       @RequestParam(value = "paid", required = false) Boolean paid,
                                       @RequestParam(value = "rangeStart", required = false)
                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                       @RequestParam(value = "rangeEnd", required = false)
                                       @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                       @RequestParam(value = "onlyAvailable", required = false, defaultValue = "false") boolean onlyAvailable,
                                       @RequestParam(value = "sort", required = false) SortType sort,
                                       @RequestParam(value = "from", required = false, defaultValue = "0") int from,
                                       @RequestParam(value = "size", required = false, defaultValue = "10") int size,
                                       HttpServletRequest request) {
    GetEventPublicParam params = new GetEventPublicParam()
        .setText(text)
        .setCategories(categories)
        .setPaid(paid)
        .setRangeStart(rangeStart)
        .setRangeEnd(rangeEnd)
        .setOnlyAvailable(onlyAvailable)
        .setSort(sort)
        .setFrom(from)
        .setSize(size);

    log.info("Request received GET /events with params {}", params);
    List<EventShortDto> events = eventService.getEvents(params, request);
    log.info("Events received: {}", events);
    return events;
  }

  @GetMapping("/{eventId}")
  public EventFullDto getEventsById(@RequestHeader(USER_ID_HEADER) Long userId,
                                    @PathVariable Long eventId) {
    log.info("Request received GET /events with id {}", eventId);
    EventFullDto event = eventService.getPublishedEventWithTracking(eventId, userId);
    log.info("Event received: {}", event);
    return event;
  }

  @PutMapping("/{eventId}/like")
  public void addLike(@RequestHeader(USER_ID_HEADER) Long userId, @PathVariable Long eventId) {
    log.info("Request received PUT /events/{}/like from user with ID {}.", eventId, userId);
    eventService.processLike(userId, eventId);
    log.info("Event Like successfully registered for event{} from user {}.", eventId, userId);
  }

  @GetMapping("/recommendations")
  public List<EventShortDto> getRecommendations(@RequestHeader(USER_ID_HEADER) Long userId,
                                                @RequestParam(value = "maxResults", defaultValue = "10") Integer maxResults) {
    log.info("Request received GET /events/recommendations to get event recommendations for user ID {}.", userId);
    List<EventShortDto> events = eventService.getRecommendations(userId, maxResults);
    log.info("Returning {} events to recommend for user {}.", events.size(), userId);
    return events;
  }
}
