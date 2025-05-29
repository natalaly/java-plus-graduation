package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Data;

/**
 * Isnused in the ADMIN API - as REQUEST body
 * <p>
 *   PATCH /admin/compilations/{compId}
 */
@Data
public class UpdateCompilationRequest {

  private Set<Long> events;

  private Boolean pinned;

  @Size(min = 1, max = 50, message = "Title must be between 1 and 50 characters")
  String title;
}
