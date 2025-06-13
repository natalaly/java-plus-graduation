package ru.practicum.exception.handler;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import ru.practicum.exception.AlreadyExistsException;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.dto.ApiError;

/**
 * Global API exception handler responsible for catching any uncaught {@link Exception} and
 * converting it into standardized {@link ApiError} JSON responses.
 */

@Slf4j
public class ErrorHandler {

  @ExceptionHandler({
      MethodArgumentNotValidException.class,
      MissingServletRequestParameterException.class,
      BadRequestException.class,
      HandlerMethodValidationException.class,
      ConstraintViolationException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiError> handleBadRequestExceptions(final Exception exception) {
    if (exception instanceof BadRequestException badRequestEx) {
      log.error("400 Bad Request: {}", badRequestEx.getMessage(), badRequestEx);
    } else {
      log.warn("400 Bad Request: {}", exception.getMessage(), exception);
    }
    return buildErrorResponse(exception, HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ApiError> handleNotFoundException(final NotFoundException exception) {
    return buildErrorResponse(exception, HttpStatus.NOT_FOUND, exception.getMessage());
  }

  @ExceptionHandler({ConflictException.class, AlreadyExistsException.class})
  @ResponseStatus(HttpStatus.CONFLICT)
  public ResponseEntity<ApiError> handleAlreadyExistsException(final RuntimeException exception) {
    return buildErrorResponse(exception, HttpStatus.CONFLICT, exception.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ApiError> handleGenericException(final Exception e) {
    return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
  }

  private ResponseEntity<ApiError> buildErrorResponse(final Exception exception, HttpStatus status,
                                                      String reason) {
    log.error("{}: {}", status.value(), reason, exception);

    final String stackTrace = ExceptionUtils.getStackTrace(exception);

    ApiError apiError = new ApiError(
        status,
        reason,
        exception.getMessage(),
        LocalDateTime.now(),
        stackTrace
    );
    log.warn("API Error: {}, {} - {}", status, exception.getMessage(), reason, exception);
    return ResponseEntity.status(status).body(apiError);
  }
}
