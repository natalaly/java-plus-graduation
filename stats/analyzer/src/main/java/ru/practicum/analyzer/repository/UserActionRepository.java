package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.analyzer.model.UserAction;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {

}
