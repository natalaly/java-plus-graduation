package ru.practicum.client;

import com.google.protobuf.Timestamp;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.action.ActionTypeProto;
import ru.practicum.ewm.stats.action.UserActionProto;
import ru.practicum.ewm.stats.collector.UserActionControllerGrpc;

@Component
@Slf4j
public class CollectorClient {

  @GrpcClient("collector")
  private UserActionControllerGrpc.UserActionControllerBlockingStub client;


  public void collectUserAction(long userId, long eventId, ActionTypeProto actionType) {
    Instant now = Instant.now();
    UserActionProto request = UserActionProto.newBuilder()
        .setUserId(userId)
        .setEventId(eventId)
        .setActionType(actionType)
        .setTimestamp(Timestamp.newBuilder()
            .setSeconds(now.getEpochSecond())
            .setNanos(now.getNano())
            .build())
        .build();
    client.collectUserAction(request);
  }
}
