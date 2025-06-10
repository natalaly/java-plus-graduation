package ru.practicum.user.controller;

import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.UserOperations;
import ru.practicum.dto.UserShortDto;
import ru.practicum.user.service.UserService;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class InternalUserController implements UserOperations {

  private final UserService userService;

  @Override
  @GetMapping("/{userId}/exists")
  public boolean existsById(@PathVariable @Positive final Long userId) {
    log.info("Received request to check if user with id {} exists.", userId);
    final Boolean userExists = userService.userExists(userId);
    log.info("User with id {} exists: {}", userId, userExists);
    return userExists;
  }

  @Override
  @GetMapping("/{userId}")
  public UserShortDto getUser(@PathVariable @Positive final Long userId) {
    log.info("Received request to get user with id {}.", userId);
    final UserShortDto user = userService.getUser(userId);
    log.info("Returning User with id {}", user.getId());
    return user;
  }

  @Override
  @GetMapping
  public List<UserShortDto> getUsers(@RequestParam(required = false) final List<Long> ids) {
    log.info("Received request to get users with IDs in {}.", ids);
    final List<UserShortDto> users = userService.getUsersInternal(ids, Pageable.unpaged());
    log.info("Returning {} Users.", users.size());
    return users;
  }
}

