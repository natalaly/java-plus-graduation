package ru.practicum.analyzer.service.similarity_processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.mapper.EventSimilarityMapper;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.model.EventSimilarityId;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.service.EventSimilarityProcessor;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityProcessorImpl implements EventSimilarityProcessor {

  private final EventSimilarityRepository repository;

  @Override
  public void process(final EventSimilarityAvro value) {
    log.debug("Saving/Updating Event Similarity Coefficient {} for events wi ID {} & {} to DB.",
        value.getScore(), value.getEventA(), value.getEventB());
    final EventSimilarity newData = EventSimilarityMapper.toEntity(value);

//    final EventSimilarityId compositeId = new EventSimilarityId(newData.getEventAId(), newData.getEventBId());

    repository.findByEventAIdAndEventBId(newData.getEventAId(), newData.getEventBId())
        .ifPresentOrElse(
            existed -> {
              log.debug(
                  "Updating existed record in the DB for events {} and {} with score {} and timestamp {}.",
                  newData.getEventAId(), newData.getEventBId(), newData.getSimilarityScore(),
                  newData.getTimestamp());
              existed.setSimilarityScore(value.getScore());
              existed.setTimestamp(value.getTimestamp());
              repository.save(existed);
            },
            () -> {
              log.debug(
                  "For giver event pair {} -{} records not found. Saving new info to the DB: score {} and timestamp {}.",
                  newData.getEventAId(), newData.getEventBId(), newData.getSimilarityScore(),
                  newData.getTimestamp());
              repository.save(newData);
            }
        );

    log.debug("Successfully processed EventSimilarity data.");

  }
}
