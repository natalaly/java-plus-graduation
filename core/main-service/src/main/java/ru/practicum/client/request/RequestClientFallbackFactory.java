package ru.practicum.client.request;

import feign.RetryableException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.ParticipationRequestDto;

@Component
@Slf4j
public class RequestClientFallbackFactory implements FallbackFactory<RequestClient> {

  @Override
  public RequestClient create(Throwable cause) {

    if (!(cause instanceof CallNotPermittedException) && !(cause instanceof RetryableException)) {
      log.warn("Fallback skipped for non-circuit-breaker exception: {}.",
          cause.getClass().getSimpleName(), cause);

      if (cause instanceof RuntimeException ex) {
        throw ex;
      } else {
        throw new RuntimeException(cause);
      }
    }

    log.warn("Request Client Fallback triggered, CAUSE: {}.", cause.getMessage(), cause);

    return new RequestClient() {

      @Override
      public List<ParticipationRequestDto> getAllEventRequests(Long eventId) {
        log.warn("Fallback: unable to call request-service - getByEventId().");
        return List.of();
      }

      @Override
      public Map<Long, List<ParticipationRequestDto>> getConfirmedRequests(List<Long> eventIds) {
        log.warn("Fallback: unable to call request-service - getConfirmedRequests().");
        return Map.of();
      }

      @Override
      public List<ParticipationRequestDto> updateRequests(List<ParticipationRequestDto> requestsToUpdate) {
        log.warn("Fallback: unable to call request-service - updateRequests().");
        return List.of();
      }

      @Override
      public List<ParticipationRequestDto> getEventPendingRequests(List<Long> requestIds, Long eventId) {
        log.warn("Fallback: unable to call request-service - getPendingRequests().");
        return List.of();
      }
    };
  }

}
