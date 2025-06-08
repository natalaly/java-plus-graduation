package ru.practicum.comment.client;

import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.EventFullDto;

@Component
@Slf4j
public class EventClientFallbackFactory implements FallbackFactory<EventClient> {

  @Override
  public EventClient create(Throwable cause) {

    if (!(cause instanceof CallNotPermittedException) && !(cause instanceof RetryableException)) {
      log.warn("Fallback skipped for non-circuit-breaker exception: {}.", cause.getClass().getSimpleName(), cause);

      if (cause instanceof RuntimeException ex) {
        throw ex;
      } else {
        throw new RuntimeException(cause);
      }
    }

    log.warn("Event Client Fallback triggered, CAUSE: {}.", cause.getMessage(), cause);

    return new EventClient() {

      @Override
      public EventFullDto getEvent(Long eventId) {
        log.warn("Fallback: unable to call event-service - getEvent().");
        return new EventFullDto();
      }

      @Override
      public boolean existsById(Long id) {
        log.warn("Fallback: unable to call event-service - existsById().");
        return false;
      }

    };
  }
}
