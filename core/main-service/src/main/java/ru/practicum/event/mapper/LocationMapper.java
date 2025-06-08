package ru.practicum.event.mapper;

import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.dto.LocationDto;
import ru.practicum.event.model.Location;

@UtilityClass
@Slf4j
public class LocationMapper {

  public static LocationDto toDto(@NotNull final Location location) {
    log.debug("Mapping Location {} to the EventFullDto.", location);
    return new LocationDto()
        .setLat(location.getLat())
        .setLon(location.getLon());
  }
}
