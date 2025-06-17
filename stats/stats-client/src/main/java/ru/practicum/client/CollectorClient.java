package ru.practicum.client;

import com.google.protobuf.Empty;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.action.UserActionProto;
import ru.practicum.ewm.stats.collector.UserActionControllerGrpc;

@Component
@Slf4j
public class CollectorClient {
  @GrpcClient("collector")
  UserActionControllerGrpc.UserActionControllerBlockingStub client;

  public void sendUserAction(UserActionProto action) {
    Empty empty = client.collectUserAction(action);
  }
}
