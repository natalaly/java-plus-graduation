package ru.practicum.analyzer.service.similarity_processor;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.mapper.EventSimilarityMapper;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.service.EventSimilarityProcessor;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventSimilarityProcessorImpl implements EventSimilarityProcessor {

  private final EventSimilarityRepository repository;

  @Override
  @Transactional
  public void process(final EventSimilarityAvro value) {
    log.debug("Saving/Updating Event Similarity Coefficient {} for events wi ID {} & {} to DB.",
        value.getScore(), value.getEventA(), value.getEventB());
    final EventSimilarity newData = EventSimilarityMapper.toEntity(value);

    repository.findByEventAIdAndEventBId(newData.getEventAId(), newData.getEventBId())
        .ifPresentOrElse(
            existed -> updateFields(existed, newData),
            () -> getSaved(newData)
        );
    log.debug("Successfully processed EventSimilarity data.");
  }

  private void getSaved(final EventSimilarity newData) {
    log.debug("For giver event pair {} -{} records not found. Saving new info to the DB: score {} and timestamp {}.",
        newData.getEventAId(), newData.getEventBId(), newData.getSimilarityScore(), newData.getTimestamp());
    repository.save(newData);
  }

  private void updateFields(final EventSimilarity existed, final EventSimilarity newData) {
    log.debug("Updating existed record in the DB for events {} and {} with score {} and timestamp {}.",
        newData.getEventAId(), newData.getEventBId(), newData.getSimilarityScore(),
        newData.getTimestamp());
    existed.setSimilarityScore(newData.getSimilarityScore());
    existed.setTimestamp(newData.getTimestamp());
    repository.save(existed);
  }
}