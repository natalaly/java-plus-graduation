package ru.practicum.comment.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.EventOperations;
import ru.practicum.comment.client.config.FeignClientConfiguration;

@FeignClient(
    name = "event-service",
    path = "/internal/events",
    configuration = FeignClientConfiguration.class,
    fallbackFactory = EventClientFallbackFactory.class)
public interface EventClient extends EventOperations {

}
