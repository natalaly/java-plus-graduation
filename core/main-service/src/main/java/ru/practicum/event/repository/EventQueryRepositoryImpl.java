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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.enums.State;
import ru.practicum.event.enums.SortType;
import ru.practicum.event.model.Event;
//TODO  MAKE DRY, SOLID, KISS

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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);

        Root<Event> eventTable = query.from(Event.class);
        eventTable.fetch("category", JoinType.LEFT);

        Predicate predicate = cb.conjunction();

        if (users != null && !users.isEmpty()) {
            predicate = cb.and(predicate, eventTable.get("initiatorId").in(users));
        }

        if (states != null && !states.isEmpty()) {
            List<State> stateEnums = states.stream()
                    .map(State::valueOf)
                    .collect(Collectors.toList());

            predicate = cb.and(predicate, eventTable.get("state").in(stateEnums));
        }

        if (categories != null && !categories.isEmpty()) {
            predicate = cb.and(predicate, eventTable.get("category").get("id").in(categories));

        }

        if (rangeStart != null) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(eventTable.get("eventDate"), rangeStart));
        }

        if (rangeEnd != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(eventTable.get("eventDate"), rangeEnd));
        }

        query.select(eventTable).where(predicate);

        TypedQuery<Event> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);

        return typedQuery.getResultList();
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> query = cb.createQuery(Event.class);

        Root<Event> eventTable = query.from(Event.class);
        eventTable.fetch("category", JoinType.LEFT);

        Predicate predicate = cb.conjunction();
        predicate = cb.and(predicate, cb.equal(eventTable.get("state"), State.PUBLISHED));

        if (text != null && !text.trim().isEmpty()) {
            String pattern = "%" + text.trim().toLowerCase() + "%";
            predicate = cb.and(predicate,
                    cb.or(
                            cb.like(cb.lower(eventTable.get("annotation")), pattern),
                            cb.like(cb.lower(eventTable.get("description")), pattern)
                    )
            );
        }

        if (categories != null && !categories.isEmpty()) {
            predicate = cb.and(predicate, eventTable.get("category").get("id").in(categories));
        }

        if (paid != null) {
            predicate = cb.and(predicate, cb.equal(eventTable.get("paid"), paid));
        }

        LocalDateTime now = LocalDateTime.now();

        if (rangeStart != null) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(eventTable.get("eventDate"), rangeStart));
        } else {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(eventTable.get("eventDate"), now));
        }

        if (rangeEnd != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(eventTable.get("eventDate"), rangeEnd));
        }

        query.where(predicate);

        if (sort != null && sort.equals(SortType.EVENT_DATE)) {
            query.orderBy(cb.asc(eventTable.get("eventDate")));
        }

        return fetchResults(query, from, size);
    }

    private List<Event> fetchResults(CriteriaQuery<Event> query, int from, int size) {
        TypedQuery<Event> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);
        return typedQuery.getResultList();
    }
}