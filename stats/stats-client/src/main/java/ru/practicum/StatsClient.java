package ru.practicum;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@FeignClient(name = "stats-server")
public interface StatsClient {

  @PostMapping("/hit")
  @ResponseStatus(HttpStatus.CREATED)
  void saveEndpointHit(@Valid @RequestBody EndPointHitDto endpointHit);

  @GetMapping("/stats")
  ResponseEntity<List<ViewStatsDto>> getStats(
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
      @RequestParam(required = false) List<String> uris,
      @RequestParam(defaultValue = "false") boolean unique
  );

}
