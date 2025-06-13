package ru.practicum.request.client.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import ru.practicum.dto.EventFullDto;

@Component
@Slf4j
public class EventClientFallbackFactory implements FallbackFactory<EventClient> {

  @Override
  public EventClient create(Throwable cause) {
    log.warn("Event Client Fallback triggered, CAUSE: {}.", cause.getMessage(), cause);

    return new EventClient() {

      @Override
      public EventFullDto getEvent(Long eventId) {
        log.warn("Fallback: unable to call event-service - getEvent().;eventID = {}.", eventId);
        return new EventFullDto();
      }

      @Override
      public boolean existsById(Long id) {
        log.warn("Fallback: unable to call event-service - existsById().; eventId = {}", id);
        return false;
      }
    };
  }
}
