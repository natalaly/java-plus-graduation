package ru.practicum.event.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.enums.State;
import ru.practicum.event.enums.SortType;
import ru.practicum.event.model.Event;

@Slf4j
@Repository
public class EventQueryRepositoryImpl implements EventQueryRepository {

  private final EntityManager entityManager;

  public EventQueryRepositoryImpl(final EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<Event> adminFindEvents(final List<Long> users,
                                     final List<String> states,
                                     final List<Long> categories,
                                     final LocalDateTime rangeStart,
                                     final LocalDateTime rangeEnd,
                                     int from,
                                     int size) {
    log.debug("Staring fetching events");

    final EventQueryFilter filter = EventQueryFilter.builder()
        .users(users)
        .states(states)
        .categories(categories)
        .rangeStart(rangeStart)
        .rangeEnd(rangeEnd)
        .from(from)
        .size(size)
        .publicEvents(false)
        .build();

    return findEventsByFilter(filter);
  }

  @Override
  public List<Event> publicGetPublishedEvents(final String text,
                                              final List<Long> categories,
                                              final Boolean paid,
                                              final LocalDateTime rangeStart,
                                              final LocalDateTime rangeEnd,
                                              final Boolean onlyAvailable,
                                              final SortType sort,
                                              final int from,
                                              final int size) {

    log.debug("Staring fetching published events");
    final EventQueryFilter filter = EventQueryFilter.builder()
        .text(text)
        .categories(categories)
        .paid(paid)
        .rangeStart(rangeStart)
        .rangeEnd(rangeEnd)
        .sort(sort)
        .from(from)
        .size(size)
        .publicEvents(true)
        .build();

    return findEventsByFilter(filter);
  }

  private List<Event> findEventsByFilter(final EventQueryFilter filter) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Event> query = cb.createQuery(Event.class);

    Root<Event> eventTable = query.from(Event.class);
    eventTable.fetch("category", JoinType.LEFT);

    Predicate predicate = cb.conjunction();
    predicate = cb.and(predicate, createUsersPredicate(cb, eventTable, filter));
    predicate = cb.and(predicate, createStatesPredicate(cb, eventTable, filter));
    predicate = cb.and(predicate, createCategoriesPredicate(cb, eventTable, filter));
    predicate = cb.and(predicate, createDateRangePredicate(cb, eventTable, filter));
    predicate = cb.and(predicate, createTextSearchPredicate(cb, eventTable, filter));
    predicate = cb.and(predicate, createPaidPredicates(cb, eventTable, filter));

    query.select(eventTable).where(predicate);
    applySorting(query, cb, eventTable, filter.getSort());

    return fetchResults(query, filter.getFrom(), filter.getSize());
  }

  private Predicate createUsersPredicate(CriteriaBuilder cb, Root<Event> root,
                                         EventQueryFilter filter) {
    return filter.getUsers() != null && !filter.getUsers().isEmpty()
        ? root.get("initiatorId").in(filter.getUsers())
        : cb.conjunction();
  }

  private Predicate createStatesPredicate(CriteriaBuilder cb, Root<Event> root,
                                          EventQueryFilter filter) {
    if (filter.isPublicEvents()) {
     return cb.equal(root.get("state"), State.PUBLISHED);
    }

    if (filter.getStates() == null || filter.getStates().isEmpty()) {
      return cb.conjunction();
    }

    List<State> stateEnums = filter.getStates().stream().map(State::valueOf).toList();
    return root.get("state").in(stateEnums);
  }

  private Predicate createCategoriesPredicate(CriteriaBuilder cb, Root<Event> root,
                                              EventQueryFilter filter) {
    return filter.getCategories() != null && !filter.getCategories().isEmpty()
        ? root.get("category").get("id").in(filter.getCategories())
        : cb.conjunction();
  }

  private Predicate createDateRangePredicate(CriteriaBuilder cb, Root<Event> root,
                                             EventQueryFilter filter) {
    Predicate predicate = cb.conjunction();
    LocalDateTime now = LocalDateTime.now();

    if (filter.getRangeStart() != null) {
      predicate = cb.and(predicate,
          cb.greaterThanOrEqualTo(root.get("eventDate"), filter.getRangeStart()));
    } else if (filter.isPublicEvents()) {
      predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("eventDate"), now));
    }

    if (filter.getRangeEnd() != null) {
      predicate = cb.and(predicate,
          cb.lessThanOrEqualTo(root.get("eventDate"), filter.getRangeEnd()));
    }
    return predicate;
  }

  private Predicate createTextSearchPredicate(CriteriaBuilder cb, Root<Event> root,
                                              EventQueryFilter filter) {
    if (filter.getText() != null && !filter.getText().trim().isEmpty()) {
      String pattern = "%" + filter.getText().trim().toLowerCase() + "%";
     return cb.or(
          cb.like(cb.lower(root.get("annotation")), pattern),
          cb.like(cb.lower(root.get("description")), pattern)
      );
    }
    return cb.conjunction();
  }

  private Predicate createPaidPredicates(CriteriaBuilder cb, Root<Event> root,
                                     EventQueryFilter filter) {
    return filter.getPaid() != null
        ? cb.equal(root.get("paid"), filter.getPaid())
        : cb.conjunction();
  }


  private List<Event> fetchResults(CriteriaQuery<Event> query, int from, int size) {
    TypedQuery<Event> typedQuery = entityManager.createQuery(query);
    typedQuery.setFirstResult(from);
    typedQuery.setMaxResults(size);
    return typedQuery.getResultList();
  }

  private void applySorting(CriteriaQuery<Event> query, CriteriaBuilder cb,
                            Root<Event> root, SortType sort) {
    if (sort == null) {
      return;
    }
    if (sort == SortType.EVENT_DATE) {
      query.orderBy(cb.asc(root.get("eventDate")));
    }
  }

  @Data
  @Builder
  private static class EventQueryFilter {

    private List<Long> users;
    private List<String> states;
    private List<Long> categories;
    private Boolean paid;
    private String text;
    private boolean publicEvents;
    private SortType sort;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private int from;
    private int size;
  }
}
