package ru.practicum.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "event_similarity")
@IdClass(EventSimilarityId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSimilarity {
  @Id
  @Column(name = "event_a_id", nullable = false)
  private Long eventAId;

  @Id
  @Column(name = "event_b_id", nullable = false)
  private Long eventBId;

  @Column(name = "similarity_score", nullable = false)
  private Double similarityScore;

  @Column(name = "timestamp", nullable = false)
  private Instant timestamp;

}
