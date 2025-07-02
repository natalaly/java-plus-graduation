package ru.practicum.client;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.recommendation.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.recommendation.RecommendedEventProto;
import ru.practicum.ewm.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.recommendation.UserPredictionsRequestProto;

@Component
@Slf4j
public class AnalyzerClient {
  @GrpcClient("analyzer")
  private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

  public Stream<RecommendedEventProto> getRecommendationsForUser(long userId, int maxResults) {
    UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
        .setUserId(userId)
        .setMaxResults(maxResults)
        .build();
    log.debug("Sending recommendations request for userID={} with maxResults={}.", userId, maxResults);
    Iterator<RecommendedEventProto> iterator = client.getRecommendationsForUser(request);
    log.debug("Received recommendation stream for userID={}.", userId);
    return asStream(iterator);
  }

  public Stream<RecommendedEventProto> getSimilarEvents(long eventId, long userId, int maxResults) {
    SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
        .setEventId(eventId)
        .setUserId(userId)
        .setMaxResults(maxResults)
        .build();
    log.debug("Sending similar events request for eventID={} and userID={} with maxResults={}.", eventId, userId, maxResults);
    Iterator<RecommendedEventProto> iterator = client.getSimilarEvents(request);
    log.debug("Received recommended events stream for eventID={} and userID={}.", eventId, userId);
    return asStream(iterator);
  }

  public Stream<RecommendedEventProto> getInteractionsCount(List<Long> eventIds) {
    InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
        .addAllEventId(eventIds)
        .build();
    log.debug("Sending interactions count request for events with IDs={}.", eventIds);
    Iterator<RecommendedEventProto> iterator = client.getInteractionsCount(request);
    log.debug("Received event stream for events with IDs={}.", eventIds);
    return asStream(iterator);
  }

  private Stream<RecommendedEventProto> asStream(Iterator<RecommendedEventProto> iterator) {
    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
        false
    );
  }
}
