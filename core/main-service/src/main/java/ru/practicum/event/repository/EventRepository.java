package ru.practicum.event.repository;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.enums.State;
import ru.practicum.event.model.Event;

public interface EventRepository extends JpaRepository<Event, Long>, EventQueryRepository {

  Page<Event> findAllByInitiatorId(Long initiatorId, PageRequest page);

  @Query("""
      SELECT e
      FROM Event e
      JOIN FETCH e.category c
      WHERE e.id = :eventId
        AND e.initiatorId = :initiatorId
      """)
  Optional<Event> findByIdAndInitiatorId(@Param("eventId") Long eventId,
                                         @Param("initiatorId") Long initiatorId);

  Optional<Event> findByIdAndState(Long id, State state);

  boolean existsByCategoryId(Long id);

  Set<Event> findAllDistinctByIdIn(Set<Long> eventIds);
}

