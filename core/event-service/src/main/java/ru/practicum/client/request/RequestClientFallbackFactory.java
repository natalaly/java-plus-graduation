package ru.practicum.client.request;

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
    log.warn("Request Client Fallback triggered, CAUSE: {}.", cause.getMessage(), cause);

    return new RequestClient() {

      @Override
      public List<ParticipationRequestDto> getAllEventRequests(Long eventId) {
        log.warn("Fallback: unable to call request-service - getByEventId(). Event ID ={}.", eventId);
        return List.of();
      }

      @Override
      public Map<Long, List<ParticipationRequestDto>> getConfirmedRequests(List<Long> eventIds) {
        log.warn("Fallback: unable to call request-service - getConfirmedRequests(). Event IDs: {}.", eventIds);
        return Map.of();
      }

      @Override
      public List<ParticipationRequestDto> updateRequests(List<ParticipationRequestDto> requestsToUpdate) {
        log.warn("Fallback: unable to call request-service - updateRequests(). Requests: {}.", requestsToUpdate);
        return List.of();
      }

      @Override
      public List<ParticipationRequestDto> getEventPendingRequests(List<Long> requestIds, Long eventId) {
        log.warn("Fallback: unable to call request-service - getPendingRequests(). Request IDs: {}, Event ID = {}.",
            requestIds, eventId);
        return List.of();
      }
    };
  }

}
