package ru.practicum.analyzer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.analyzer.model.EventSimilarity;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

  Optional<EventSimilarity> findByEventAIdAndEventBId(long eventA, long eventB);

//  Optional<EventSimilarity> findById(EventSimilarityId id);
}
