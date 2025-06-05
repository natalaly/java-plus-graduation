package ru.practicum.user.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

  private final UserService userService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserDto createUser(@Validated @RequestBody UserDto userDto) {
    log.info("Received request to create user: {}", userDto);
    UserDto createdUser = userService.addUser(userDto);
    log.info("User created with ID: {}", createdUser.getId());
    return createdUser;
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable @NotNull @Positive Long userId) {
    log.info("Received request to delete user with ID: {}", userId);
    userService.deleteUser(userId);
    log.info("User with ID {} deleted", userId);
  }

  @GetMapping
  public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                @RequestParam(defaultValue = "0") int from,
                                @RequestParam(defaultValue = "10") int size) {
    log.info("Received request to get users. IDs: {}, from: {}, size: {}", ids, from, size);
    Pageable pageable = PageRequest.of(from / size, size);
    List<UserDto> users = userService.getUsers(ids, pageable);
    log.info("Returning {} users", users.size());
    return users;
  }
}
