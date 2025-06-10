package ru.practicum.request.client.event;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.EventOperations;
import ru.practicum.request.client.config.FeignClientConfiguration;
//TODO CHANGE TO EVENT_SERVICE WHRN IR WILL NE READY

@FeignClient(
    name = "main-service",
    path = "/internal/events",
    configuration = FeignClientConfiguration.class,
    fallbackFactory = EventClientFallbackFactory.class)
public interface EventClient extends EventOperations {

}
