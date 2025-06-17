package ru.practicum.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.collector.handler.UserActionHandler;
import ru.practicum.ewm.stats.action.UserActionProto;
import ru.practicum.ewm.stats.collector.UserActionControllerGrpc;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {

  private final UserActionHandler handler;

  @Override
  public void collectUserAction(final UserActionProto request,
                                final StreamObserver<Empty> responseObserver) {
    log.info("Received user action: userID={}, eventID={}, actionType={}",
        request.getUserId(), request.getEventId(), request.getActionType());
    try {
      handler.handle(request);
      log.info("Successfully processed user action: userID={}, eventID={}, actionType={}",
          request.getUserId(), request.getEventId(), request.getActionType());

      responseObserver.onNext(Empty.getDefaultInstance());
      responseObserver.onCompleted();
    } catch (Exception e) {
      log.error("Error while processing user action: userID={}, eventID={}, actionType={}",
          request.getUserId(), request.getEventId(), request.getActionType());
      responseObserver.onError(new StatusRuntimeException(
          Status.INTERNAL
              .withDescription(e.getLocalizedMessage())
              .withCause(e)
      ));
    }
  }
}
