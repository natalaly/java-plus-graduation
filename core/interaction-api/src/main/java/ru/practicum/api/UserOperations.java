package ru.practicum.api;

import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.UserShortDto;


public interface UserOperations {

  @GetMapping("/{userId}/exists")
  boolean existsById(@PathVariable @Positive Long userId);

  @GetMapping("/{userId}")
  UserShortDto getUser(@PathVariable @Positive Long userId);

  @GetMapping
  List<UserShortDto> getUsers(@RequestParam(required = false) List<Long> ids);

}
