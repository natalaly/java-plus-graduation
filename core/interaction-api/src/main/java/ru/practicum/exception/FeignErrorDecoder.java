package ru.practicum.exception;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.exception.dto.ApiError;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

  private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  @Override
  public Exception decode(String methodKey, Response response) {
    try {
      ApiError apiError = objectMapper.readValue(response.body().asInputStream(), ApiError.class);

      return switch (response.status()) {
        case 400 -> new BadRequestException(apiError.message());
        case 404 -> new NotFoundException(apiError.message());
        case 409 -> new ConflictException(apiError.message());
        default -> new FeignException.FeignServerException(
            response.status(),
            "Error occurs during calling external service: " + apiError.message(),
            response.request(),
            response.body().asInputStream().readAllBytes(),
            Collections.emptyMap()
        );
      };
    } catch (IOException e) {
      log.error("Error occurs during decoding response body: {}", e.getMessage(), e);
      return new FeignException.FeignServerException(
          response.status(),
          "Error occurs during parsing error response: " + e.getMessage(),
          response.request(),
          null,
          Collections.emptyMap()
      );
    }
  }
}
