package ru.practicum.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import ru.practicum.api.UserOperations;
import ru.practicum.client.config.FeignClientConfiguration;

@FeignClient(
    name = "user-service",
    path = "/internal/users",
    configuration = FeignClientConfiguration.class,
    fallbackFactory = UserClientFallbackFactory.class)
public interface UserClient extends UserOperations {

}
