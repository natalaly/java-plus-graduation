package ru.practicum.analyzer.service.user_processor;

import jakarta.transaction.Transactional;
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

  @Transactional
  @Override
  public void process(final UserActionAvro value) {
    log.debug("Saving/Updating new User Activity data: userID {}, eventID {}, action performed:{}.",
        value.getUserId(), value.getEventId(), value.getActionType().name());

    final UserAction newData = UserActionMapper.toEntity(value);

    repository.findByUserIdAndEventId(newData.getUserId(), newData.getEventId())
        .ifPresentOrElse(
            existedRecord -> updateFields(existedRecord,newData),
            () -> getSave(newData)
        );
    log.debug("Successfully processed UserActivity data: {}.", newData);
  }

  private void getSave(final UserAction newData) {
    log.trace("Saving new record to DB: userID={}, eventID={}, ActionType={}, weight={}, timestamp={}.",
        newData.getUserId(), newData.getEventId(), newData.getActionType(), newData.getWeight(), newData.getTimestamp());
     repository.save(newData);
  }

  private void updateFields(final UserAction existedRecord, final UserAction newData) {
    log.debug("Updating existing record in the DB for userID {} and eventID {}."
            + " New weight: {}, Existing weight: {}, Timestamp:{}.",
        newData.getUserId(), newData.getEventId(), newData.getWeight(),existedRecord.getWeight(), newData.getTimestamp());
    if (newData.getWeight() > existedRecord.getWeight()) {
      existedRecord.setWeight(newData.getWeight());
      existedRecord.setActionType(newData.getActionType());
      log.trace("Action weight increased, existing record updated with type={}, weight={}, and timestamp={}.",
          newData.getActionType(), newData.getWeight(), newData.getTimestamp());
    } else {
      log.trace("Weight not increased — updated timestamp only.");
    }
    existedRecord.setTimestamp(newData.getTimestamp());
    repository.save(existedRecord);
    log.debug("Successfully updated existing record in the DB for userID {} and eventID {}.",
        newData.getUserId(), newData.getEventId());
  }
}