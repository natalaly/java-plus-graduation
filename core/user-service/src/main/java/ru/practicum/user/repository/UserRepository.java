package ru.practicum.user.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

  Page<User> findAllByIdIn(List<Long> ids, Pageable pageable);

}
