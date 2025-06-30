package ru.practicum.analyzer.service.recommendation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.mapper.EventSimilarityMapper;
import ru.practicum.analyzer.mapper.UserActionMapper;
import ru.practicum.analyzer.model.EventSimilarity;
import ru.practicum.analyzer.model.RecommendedEvent;
import ru.practicum.analyzer.model.UserAction;
import ru.practicum.analyzer.repository.EventSimilarityRepository;
import ru.practicum.analyzer.repository.UserActionRepository;
import ru.practicum.analyzer.service.RecommendationsService;
import ru.practicum.ewm.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.recommendation.RecommendedEventProto;
import ru.practicum.ewm.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.recommendation.UserPredictionsRequestProto;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationsServiceImpl implements RecommendationsService {

  private static final int N_RECENT_INTERACTIONS_LIMIT = 20;
  private static final int K_NEAREST_NEIGHBORS = 5;

  private final EventSimilarityRepository eventSimilarityRepository;
  private final UserActionRepository userActionRepository;

  @Override
  public List<RecommendedEventProto> getRecommendationsForUser(
      final UserPredictionsRequestProto request) {

    final Long userId = request.getUserId();
    int maxResults = (int) request.getMaxResults();
    log.debug("Generating recommendations for user ID: {} with maxResults: {}", userId, maxResults);

//    Stage 1: Selecting events, given user has not yet interacted with

//    1. Getting recent interactions (limit to N_RECENT_INTERACTIONS_LIMIT)
    final List<UserAction> recentUserActions = getRecentUserInteractions(userId);
    if (recentUserActions.isEmpty()) {
      log.info("User ID {} has no recent interactions. Returning empty recommendations.", userId);
      return List.of();
    }

    final Set<Long> recentEventIds = recentUserActions.stream()
        .map(UserAction::getEventId)
        .collect(Collectors.toSet());

//    2. Get all interacted event IDs for filtering
    final List<UserAction> allUserActions = userActionRepository.findAllByUserId(userId);
    final Set<Long> allInteractedEventIds = allUserActions.stream()
        .map(UserAction::getEventId)
        .collect(Collectors.toSet());

//    3. Fetching new N(maxResults*2) similarities to the recentUserActions events, sorted by similarity score
    final List<EventSimilarity> similaritiesToUnseenEvents =
        eventSimilarityRepository.findByEventIdInAndExcludedEventIds(
            recentEventIds,
            allInteractedEventIds,
            PageRequest.of(0, N_RECENT_INTERACTIONS_LIMIT));

//    Stage 2: Predicted Rating Computing

//    4. Group by candidate event ID (unseen event)
    Map<Long, List<EventSimilarity>> similaritiesByCandidate = similaritiesToUnseenEvents.stream()
        .collect(Collectors.groupingBy(sim -> {
          return allInteractedEventIds.contains(sim.getEventAId())
              ? sim.getEventBId()  // candidate
              : sim.getEventAId(); // candidate
        }));

//    5. Map user weights for known events
    Map<Long, Double> userWeights = allUserActions.stream()
        .collect(Collectors.toMap(UserAction::getEventId, UserAction::getWeight));

//    6. Compute the predicted score for each candidate

    final List<RecommendedEventProto> predictions = computePredictedScores(
        similaritiesByCandidate, userWeights, maxResults);
    log.debug("Returning {} predicted recommendations for user ID {}", predictions.size(), userId);
    return predictions;
  }

  @Override
  public List<RecommendedEventProto> getSimilarEvents(final SimilarEventsRequestProto request) {
    log.debug("Getting new similar to the eventID={} events, for userID={}, max result size={} .",
        request.getEventId(), request.getUserId(), request.getMaxResults());
    final Long baseEventId = request.getEventId();
    final Long userId = request.getUserId();
    final int limit = (int) request.getMaxResults();

    return fetchSimilarEvents(baseEventId, userId, limit);
  }

  @Override
  public List<RecommendedEventProto> getInteractionsCount(
      final InteractionsCountRequestProto request) {
    final List<Long> eventIds = request.getEventIdList();
    log.debug("Starting retrieving Interactions count information for event IDS: {} ", eventIds);
    if (eventIds.isEmpty()) {
      log.debug("No Ids provided for Interactions count request. Returning empty list.");
      return List.of();
    }
    final List<RecommendedEvent> events = userActionRepository.getEventsInteractionsCount(eventIds);
    log.trace("Found {} events interactions count information for event IDS{}: {} ",
        events.size(), eventIds, events);

    return events.stream()
        .map(UserActionMapper::toRecommendedEventProto)
        .toList();
  }

  private List<RecommendedEventProto> computePredictedScores(
      final Map<Long, List<EventSimilarity>> similaritiesByCandidate,
      final Map<Long, Double> userWeights,
      final int maxResults) {
    log.debug("Starting computing predicted scores for {} candidates.",
        similaritiesByCandidate.size());
    return similaritiesByCandidate.entrySet().stream()
        .map(entry -> predictScoreForCandidate(
            entry.getKey(), entry.getValue(), userWeights))
        .sorted(Comparator.comparingDouble(RecommendedEventProto::getScore).reversed())
        .limit(maxResults)
        .toList();
  }

  private RecommendedEventProto predictScoreForCandidate(
      final Long candidateId,
      final List<EventSimilarity> neighbors,
      final Map<Long, Double> userWeights) {
    log.trace(
        "Starting computing predicted score for candidate event ID: {}, based on similarities data {}.",
        candidateId, neighbors);
    final List<EventSimilarity> topKNeighbors = neighbors.stream()
        .filter(sim -> {
          Long neighborId = getNeighborId(candidateId, sim);
          return userWeights.containsKey(neighborId);
        })
        .sorted(Comparator.comparing(EventSimilarity::getSimilarityScore).reversed())
        .limit(K_NEAREST_NEIGHBORS)
        .toList();

    double numerator = 0.0;
    double denominator = 0.0;

    for (EventSimilarity sim : topKNeighbors) {
      Long neighborId = getNeighborId(candidateId, sim);
      Double weight = userWeights.get(neighborId);
      double similarity = sim.getSimilarityScore();

      numerator += weight * similarity;
      denominator += similarity;
    }

    final Double predictedScore = (denominator > 0) ? (numerator / denominator) : 0.0;
    log.trace("Predicted score for candidate event ID: {} is {}.", candidateId, predictedScore);
    return RecommendedEventProto.newBuilder()
        .setEventId(candidateId)
        .setScore(predictedScore)
        .build();
  }

  private Long getNeighborId(final Long candidateId, final EventSimilarity sim) {
    return candidateId.equals(sim.getEventAId())
        ? sim.getEventBId()
        : sim.getEventAId();
  }


  private List<UserAction> getRecentUserInteractions(final Long userId) {
    log.debug("Getting recent user interactions for user ID: {} with maxResults: {}", userId,
        N_RECENT_INTERACTIONS_LIMIT);
    final PageRequest pageable = PageRequest.of(0, N_RECENT_INTERACTIONS_LIMIT);
    final List<UserAction> result = userActionRepository.findByUserIdOrderByTimestampDesc(userId,
        pageable);
    log.trace("Found {} recent user interactions for user ID: {}.", result.size(), userId);
    return result;
  }

  private List<RecommendedEventProto> fetchSimilarEvents(final Long baseEventId,
                                                         final Long userId,
                                                         final int limit) {
    final PageRequest pageable = PageRequest.of(0, limit);
    final Set<Long> interactedEvents = getInteractedEvents(userId);

    final List<EventSimilarity> topUnseenSimilarities =
        eventSimilarityRepository.findTopByEventIdAndExcludedEventIds(baseEventId, interactedEvents,
            pageable);

    log.trace(
        "Found {} events, where one of the event is baseEventId={} and other not interacted with user {}. ",
        topUnseenSimilarities.size(), baseEventId, userId);

    return EventSimilarityMapper.mapToRecommendedEventProto(
        baseEventId,
        topUnseenSimilarities);
  }

  private Set<Long> getInteractedEvents(Long userId) {
    final Set<Long> result = userActionRepository.findAllByUserId(userId)
        .stream()
        .map(UserAction::getEventId)
        .collect(Collectors.toSet());
    log.trace("Found all events the userId{} has interacted with: {}.", userId, result);
    return result;
  }
}
