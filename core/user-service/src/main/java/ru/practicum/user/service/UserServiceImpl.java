package ru.practicum.user.service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.UserShortDto;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mappers.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public UserDto addUser(UserDto userDto) {
    User user = UserMapper.mapToUser(userDto);
    return UserMapper.mapToUserDto(save(user));
  }

  @Override
  @Transactional
  public void deleteUser(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("User not found with id: " + userId);
    }
    userRepository.deleteById(userId);
  }

  @Override
  @Transactional
  public List<UserDto> getUsers(List<Long> ids, Pageable pageable) {

    List<User> users = findUsersByIds(ids, pageable);

    return users.stream()
        .map(UserMapper::mapToUserDto)
        .collect(Collectors.toList());
  }

  @Override
  public List<UserShortDto> getUsersInternal(List<Long> ids, Pageable unpaged) {
    List<User> users = findUsersByIds(ids, unpaged);
    return users.stream()
        .map(UserMapper::mapToUserShortDto)
        .toList();
  }

  @Override
  public UserShortDto getUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() ->
            new NotFoundException("User not found with id: " + userId));
    return UserMapper.mapToUserShortDto(user);
  }

  @Override
  public Boolean userExists(final Long userId) {
    Objects.requireNonNull(userId, "User ID must not be null.");
    return userRepository.existsById(userId);
  }

  private User save(User user) {
    log.debug("Saving user with email: {}", user.getEmail());

    try {
      return userRepository.save(user);
    } catch (DataIntegrityViolationException e) {
      log.warn("Failed to save user. Email already exists: {}", user.getEmail());
      throw new AlreadyExistsException("Email already exists.");
    }
  }

  private List<User> findUsersByIds(List<Long> ids, Pageable pageable) {
    Page<User> users;
    if (ids != null && !ids.isEmpty()) {
      users = userRepository.findAllByIdIn(ids, pageable);
    } else {
      users = userRepository.findAll(pageable);
    }
    return users.getContent();
  }
}
