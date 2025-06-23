package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CollectorClient {
//
//  @GrpcClient("collector")
//  private UserActionControllerGrpc.UserActionControllerBlockingStub userActionController;
//
//  public void sendPreviewEvent(long userId, long eventId) {
//    userActionController.collectUserAction(getUserActionProto(userId, eventId, ActionTypeProto.ACTION_VIEW));
//  }
//
//  public void sendRegistrationEvent(long userId, long eventId) {
//    userActionController.collectUserAction(getUserActionProto(userId, eventId, ActionTypeProto.ACTION_REGISTER));
//  }
//
//  public void sendLikeEvent(long userId, long eventId) {
//    userActionController.collectUserAction(getUserActionProto(userId, eventId, ActionTypeProto.ACTION_LIKE));
//  }
//
//  private UserActionProto getUserActionProto(long userId, long eventId, ActionTypeProto actionType) {
//    return UserActionProto.newBuilder()
//        .setActionType(actionType)
//        .setUserId(userId)
//        .setEventId(eventId)
//        .setTimestamp(Timestamp.newBuilder()
//            .setSeconds(Instant.now().getEpochSecond())
//            .setNanos(Instant.now().getNano())
//            .build())
//        .build();
//  }

}
