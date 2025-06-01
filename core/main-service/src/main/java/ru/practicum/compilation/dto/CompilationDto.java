package ru.practicum.compilation.dto;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.event.dto.EventShortDto;

/**
 * Is used in the Admin API - as RESPONSE
 * <p>
 * POST /admin/compilations
 */
@Data
@Accessors(chain = true)
public class CompilationDto {

  private Set<EventShortDto> events = new HashSet<>();

  private Long id;

  private Boolean pinned;

  private String title;

}
