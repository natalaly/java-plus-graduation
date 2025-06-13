package ru.practicum.exception.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;

public record ApiError(
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    HttpStatus status,
    String reason,
    String message,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime timestamp,
    String errors) {

    @JsonCreator
    public ApiError(
        @JsonProperty("status") Object status,
        @JsonProperty("reason") String reason,
        @JsonProperty("message") String message,
        @JsonProperty("timestamp") LocalDateTime timestamp,
        @JsonProperty("errors") String errors) {
        this(convertToHttpStatus(status), reason, message, timestamp, errors);
    }

    private static HttpStatus convertToHttpStatus(Object status) {
        if (status instanceof Integer) {
            return HttpStatus.resolve((Integer) status);
        } else if (status instanceof String) {
            try {
                return HttpStatus.valueOf((String) status);
            } catch (IllegalArgumentException e) {
                return HttpStatus.valueOf(Integer.parseInt((String) status));
            }
        } else if (status instanceof HttpStatus) {
            return (HttpStatus) status;
        }
        throw new IllegalArgumentException("Invalid status value: " + status);
    }


}
