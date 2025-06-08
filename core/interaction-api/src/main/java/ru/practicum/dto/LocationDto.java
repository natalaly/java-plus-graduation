package ru.practicum.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class LocationDto {

  @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90.")
  @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90.")
  private Float lat;

  @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180.")
  @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180.")
  private Float lon;
}