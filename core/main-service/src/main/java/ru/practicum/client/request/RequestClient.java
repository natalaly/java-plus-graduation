package ru.practicum.client.request;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.RequestOperations;
import ru.practicum.client.config.FeignClientConfiguration;

@FeignClient(
    name = "request-service",
    path = "/internal/requests",
    configuration = FeignClientConfiguration.class,
    fallbackFactory = RequestClientFallbackFactory.class)
public interface RequestClient extends RequestOperations {


}
