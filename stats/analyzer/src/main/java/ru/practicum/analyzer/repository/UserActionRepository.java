package ru.practicum.analyzer.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.analyzer.model.RecommendedEvent;
import ru.practicum.analyzer.model.UserAction;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {

  @Query("""
      SELECT ua.eventId AS eventId, SUM(ua.weight) AS score
      FROM UserAction ua
      WHERE ua.eventId IN :eventIds
      GROUP BY ua.eventId
      """)
  List<RecommendedEvent> getEventsInteractionsCount(@Param("eventIds") List<Long> eventIds);

  List<UserAction> findAllByUserId(Long userId);

  @Query("""
      SELECT ua
      FROM UserAction ua
      WHERE ua.userId = :userId
      ORDER BY ua.timestamp DESC
      """)
  List<UserAction> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId,
                                                    PageRequest pageable);

  Optional<UserAction> findByUserIdAndEventId(Long userId, Long eventId);
}
