package ru.practicum.user.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.UserShortDto;
import ru.practicum.user.dto.UserDto;

public interface UserService {

  UserDto addUser(UserDto userDto);

  void deleteUser(Long userId);

  List<UserDto> getUsers(List<Long> ids, Pageable pageable);

  List<UserShortDto> getUsersInternal(List<Long> ids, Pageable unpaged);

  UserShortDto getUser(Long userId);

  Boolean userExists(Long userId);


}
