package ru.practicum.exception.decoder;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.dto.ApiError;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

  private final ErrorDecoder defaultDecoder = new Default();

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  @Override
  public Exception decode(final String methodKey, final Response response) {

    if (response.body() == null) {
      log.warn("Feign Client call to {} failed with status {} and empty or unknown body.",
          methodKey, response.status());
      return fallbackException(methodKey, response);
    }

    try {
      final ApiError apiError = objectMapper.readValue(response.body().asInputStream(),
          ApiError.class);
      return switch (apiError.status()) {
        case BAD_REQUEST -> new BadRequestException(apiError.message());
        case NOT_FOUND -> new NotFoundException(apiError.message());
        case CONFLICT -> new ConflictException(apiError.message());
        default -> fallbackException(methodKey, response);
      };
    } catch (IOException e) {
      log.error("Error occurs during decoding response body: {}", e.getMessage(), e);
      return fallbackException(methodKey, response);
    }
  }

  private Exception fallbackException(final String methodKey, final Response response) {
    try {
      byte[] bodyBytes = response.body() != null
          ? response.body().asInputStream().readAllBytes()
          : null;
      final Response errorResponse = Response.builder()
          .status(response.status())
          .reason(response.reason())
          .headers(response.headers())
          .request(response.request())
          .body(bodyBytes)
          .build();
      return FeignException.errorStatus(methodKey, errorResponse);
    } catch (IOException e) {
      log.error("Feign default exception. MethodKey:{}, HttpStatus:{}", methodKey,
          response.status());
      return defaultDecoder.decode(methodKey, response);
    }
  }
}
