package ru.practicum.comment.client;

import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.UserShortDto;

@Component
@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

  @Override
  public UserClient create(Throwable cause) {

    if (!(cause instanceof CallNotPermittedException) && !(cause instanceof RetryableException)) {
      log.warn("Fallback skipped for non-circuit-breaker exception: {}.", cause.getClass().getSimpleName(), cause);

      if (cause instanceof RuntimeException ex) {
        throw ex;
      } else {
        throw new RuntimeException(cause);
      }
    }

    log.warn("User Client Fallback triggered, CAUSE: {}.", cause.getMessage(), cause);

    return new UserClient() {

      @Override
      public boolean existsById(Long id) {
        log.warn("Fallback: unable to call user-service - existsById().");
        return false;
      }

      @Override
      public UserShortDto getUser(Long id) {
        log.warn("Fallback: unable to call user-service - getUser().");
        return new UserShortDto(null,null);
      }

      @Override
      public List<UserShortDto> getUsers(List<Long> ids) {
        log.warn("Fallback: unable to call user-service - getUsers().");
        return ids.stream().map(id -> new UserShortDto(null,null)).toList();
      }

    };
  }
}
