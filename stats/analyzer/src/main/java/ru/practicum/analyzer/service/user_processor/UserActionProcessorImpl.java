package ru.practicum.analyzer.service.user_processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.mapper.UserActionMapper;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.repository.UserActionRepository;
import ru.practicum.analyzer.service.UserActionProcessor;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionProcessorImpl implements UserActionProcessor {

  private final UserActionRepository repository;

  @Override
  public void process(final UserActionAvro value) {
    log.debug("Saving/Updating new User Activity data: userID {}, eventID {}, action performed:{}.",
        value.getUserId(), value.getEventId(), value.getActionType().name());

    final UserAction newData = UserActionMapper.toEntity(value);
    repository.save(newData);
    log.debug("Successfully processed UserActivity data.");
  }
}
