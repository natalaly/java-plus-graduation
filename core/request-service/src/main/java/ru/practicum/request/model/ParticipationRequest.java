package ru.practicum.request.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.practicum.enums.StatusRequest;

@Entity
@Table(name = "request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
public class ParticipationRequest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id")
  private Long eventId;

  @Column(name = "user_id")
  private Long requesterId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private StatusRequest status = StatusRequest.PENDING;

  private LocalDateTime created = LocalDateTime.now();

  public ParticipationRequest(final Long userId, final Long event) {
    this.requesterId = userId;
    this.eventId = event;
  }
}
