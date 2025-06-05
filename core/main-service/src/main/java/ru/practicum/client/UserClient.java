package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.api.UserOperations;
import ru.practicum.client.config.FeignClientConfiguration;
import ru.practicum.dto.UserShortDto;

@FeignClient(
    name = "user-service",
    path = "/internal/users",
    configuration = FeignClientConfiguration.class,
    fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient extends UserOperations {

  @GetMapping("/{userId}/exists")
  boolean existsById(@PathVariable Long userId);

  @GetMapping("/{userId}")
  UserShortDto getUser(@PathVariable Long userId);

}
