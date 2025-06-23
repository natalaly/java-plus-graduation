package ru.practicum.analyzer.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.analyzer.service.RecommendationsService;
import ru.practicum.ewm.stats.recommendation.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.recommendation.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.recommendation.RecommendedEventProto;
import ru.practicum.ewm.stats.recommendation.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.recommendation.UserPredictionsRequestProto;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class RecommendationsController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {

  private final RecommendationsService recommendationService;


  /**
   * gRPC endpoint for getting personalized event recommendations for a specific user.
   * <p>
   * Recommendations are computed using collaborative filtering based on user interaction history
   * and event similarity scores. Only events the user hasn't interacted with are considered.
   */
  @Override
  public void getRecommendationsForUser(final UserPredictionsRequestProto request,
                                        final StreamObserver<RecommendedEventProto> responseObserver) {
    handleGrpcRequest(
        request,
        responseObserver,
        recommendationService::getRecommendationsForUser,
        "event recommendations for a specific user",
        "userID=" + request.getUserId());
  }

  /**
   * gRPC endpoint for retrieving events similar to a given event that the user hasn't interacted
   * with yet.
   * <p>
   * The result is based on event similarity coefficients stored in the system. The response
   * excludes any events the user has already interacted with.
   */
  @Override
  public void getSimilarEvents(final SimilarEventsRequestProto request,
                               final StreamObserver<RecommendedEventProto> responseObserver) {
    handleGrpcRequest(
        request,
        responseObserver,
        recommendationService::getSimilarEvents,
        "similar events to a given one",
        "eventID=" + request.getEventId() + ", userID=" + request.getUserId());
  }

  /**
   * gRPC endpoint for calculating total user interaction weights for a list of events.
   * <p>
   * For each requested event, returns the sum of the highest interaction weights per user,
   * reflecting event popularity or engagement.
   */
  @Override
  public void getInteractionsCount(final InteractionsCountRequestProto request,
                                   final StreamObserver<RecommendedEventProto> responseObserver) {
    handleGrpcRequest(
        request,
        responseObserver,
        recommendationService::getInteractionsCount,
        "interactions count for the events",
        request.getEventIdList().toString());
  }

  private <T> void handleGrpcRequest(T request,
                                     StreamObserver<RecommendedEventProto> responseObserver,
                                     Function<T, List<RecommendedEventProto>> serviceCall,
                                     String requestName,
                                     String specificRequestData) {
    try {
      log.info("Received request for {}: {}", requestName, specificRequestData);

      List<RecommendedEventProto> events = serviceCall.apply(request);

      events.forEach(responseObserver::onNext);
      log.info("Successfully processed {} request: {}. Result: {} events.",
          requestName, specificRequestData, events.size());

      responseObserver.onCompleted();

    } catch (Exception e) {
      log.error("Error occurred during {} request processing: {} \nRequest: {}",
          requestName, e.getMessage(), request, e);
      responseObserver.onError(new StatusRuntimeException(
          Status.INTERNAL
              .withDescription(e.getLocalizedMessage())
              .withCause(e)
      ));
    }
  }
}
