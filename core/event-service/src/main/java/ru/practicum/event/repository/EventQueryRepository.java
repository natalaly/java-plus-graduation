package ru.practicum.event.repository;

import ru.practicum.event.enums.SortType;

import java.time.LocalDateTime;
import java.util.List;
import ru.practicum.event.model.Event;

public interface EventQueryRepository {

  List<Event> adminFindEvents(final List<Long> users,
                              final List<String> states,
                              final List<Long> categories,
                              final LocalDateTime rangeStart,
                              final LocalDateTime rangeEnd,
                              int from,
                              int size);

  List<Event> publicGetPublishedEvents(final String text,
                                       final List<Long> categories,
                                       final Boolean paid,
                                       final LocalDateTime rangeStart,
                                       final LocalDateTime rangeEnd,
                                       final Boolean onlyAvailable,
                                       final SortType sort,
                                       final int from,
                                       final int size
                                              );
}
