package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.event.enums.Role;
import ru.practicum.event.model.Location;
import ru.practicum.validation.MinimumHoursFromNow;
import ru.practicum.validation.ValidStateAction;

/**
 * Used in the ADMIN API - as REQUEST body
 * <p> PATCH admin/events/{eventId}
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEventAdminRequest {

  @Size(min = 20, max = 2000, message = "Annotation must be between 20 and 2000 characters.")
  private String annotation;

  @Positive(message = "Category must be a positive number.")
  private Long category;

  @Size(min = 20, max = 7000, message = "Description must be between 20 and 7000 characters.")
  private String description;

  @MinimumHoursFromNow(hoursInFuture = 1, message = "EventDAte should be at least in 1 hour from now.")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime eventDate;

  private Location location;

  private Boolean paid;

  @Positive(message = "Participant limit must be a positive number.")
  private Integer participantLimit;

  private Boolean requestModeration;

  @ValidStateAction(role = Role.ADMIN)
  private String stateAction;

  @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters.")
  private String title;

}
