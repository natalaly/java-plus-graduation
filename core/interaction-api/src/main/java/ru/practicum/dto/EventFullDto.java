package ru.practicum.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Used in APIs :
 * <ul>
 *   <li>ADMIN  - as RESPONSE</li>
 * <p>
 *     GET /admin/events PATCH admin/events/{eventId};
 * <p>
 *   <li>PUBLIC - as RESPONSE </li>
 * <p>
 *     GET /events GET /events/{id};
 * <p>
 *  <li>PRIVATE- as RESPONSE</li>
 * <p> POST /users/{userId}/events
 * <p> GET /users/{userId}/events/{eventId}
 * <p> PATCH /users/{userId}/events/{eventId}
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
public class EventFullDto {

  private String annotation;
  private CategoryDto category;
  private Integer confirmedRequests;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdOn;
  private String description;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime eventDate;
  private Long id;
  private UserShortDto initiator;
  private LocationDto location;
  private Boolean paid;
  private Integer participantLimit = 0;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime publishedOn;
  private Boolean requestModeration = true;
  private String state;
  private String title;
  private Long views;

  public EventFullDto(String annotation, CategoryDto category, Integer confirmedRequests,
                      LocalDateTime createdOn,
                      String description, LocalDateTime eventDate, Long id, UserShortDto initiator,
                      LocationDto location, Boolean paid, Integer participantLimit,
                      LocalDateTime publishedOn,
                      Boolean requestModeration, String state, String title) {
    this.annotation = annotation;
    this.category = category;
    this.confirmedRequests = confirmedRequests;
    this.createdOn = createdOn;
    this.description = description;
    this.eventDate = eventDate;
    this.id = id;
    this.initiator = initiator;
    this.location = location;
    this.paid = paid;
    this.participantLimit = participantLimit;
    this.publishedOn = publishedOn;
    this.requestModeration = requestModeration;
    this.state = state;
    this.title = title;
  }
}
