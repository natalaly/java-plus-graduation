package ru.practicum.comment.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentProcessingService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/events/{eventId}/comments")
@Slf4j
public class PublicCommentController {

  private final CommentProcessingService commentService;

  @GetMapping
  public ResponseEntity<List<CommentDto>> getByEvent(
      @PathVariable Long eventId,
      @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
      @RequestParam(defaultValue = "10") @Positive Integer size) {
    log.info("Request received GET /events/{}/comments?from={}&size={}.", eventId, from, size);
    List<CommentDto> comments = commentService.getAllEventComments(eventId, from, size);
    log.info("Sending event comments list size {}.", comments.size());
    return ResponseEntity.ok(comments);
  }
}
