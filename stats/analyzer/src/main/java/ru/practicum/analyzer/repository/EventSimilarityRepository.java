package ru.practicum.analyzer.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.analyzer.model.EventSimilarity;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

  Optional<EventSimilarity> findByEventAIdAndEventBId(long eventA, long eventB);

  @Query("""
      SELECT es
      FROM EventSimilarity es
      WHERE (es.eventAId = :eventId OR es.eventBId = :eventId)
        AND (
          CASE
            WHEN es.eventAId = :eventId THEN es.eventBId
            ELSE es.eventAId
          END
        ) NOT IN :excludedIds
      ORDER BY es.similarityScore DESC
      """)
  List<EventSimilarity> findTopByEventIdAndExcludedEventIds(@Param("eventId") Long eventId,
                                                            @Param("excludedIds") Set<Long> excludedIds,
                                                            PageRequest pageable);

  @Query("""
      SELECT es
      FROM EventSimilarity es
      WHERE
        (es.eventAId IN :includedIds AND es.eventBId NOT IN :excludedIds)
        OR
        (es.eventBId IN :includedIds AND es.eventAId NOT IN :excludedIds)
      ORDER BY es.similarityScore DESC
      """)
  List<EventSimilarity> findByEventIdInAndExcludedEventIds(@Param("includedIds")Set<Long> includedIds,
                                                           @Param(("excludedIds")) Set<Long> excludedIds,
                                                           PageRequest pageable);
}