package ru.practicum.comment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.service.CommentProcessingService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/{userId}/comments")
@Slf4j
@Validated
public class PrivateCommentController {

    private final CommentProcessingService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CommentDto> addComments(
            @PathVariable Long userId,
            @RequestParam @Positive Long eventId,
            @RequestBody @Validated CommentDto commentDto) {
        log.info("Request received POST /users/{}/comments.", userId);
        commentDto.setUserId(userId);
        commentDto.setEventId(eventId);
        CommentDto comment = commentService.addComment(commentDto);
        log.info("Comment added with ID: {}.", comment.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable @NonNull Long commentId,
            @PathVariable @NonNull Long userId) {
        log.info("Request received DELETE /users/{}/comments/{}.", userId, commentId);
        commentService.delete(userId, commentId);
        log.info("Comment with ID {} deleted.", commentId);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentDto commentDto) {
        log.info("Request received PATCH /users/{}/comments/{}.", userId, commentId);
        CommentDto comment  = commentService.updateUserComment(userId, commentId, commentDto);
        log.info("Comment with ID {} updated.", commentId);
        return ResponseEntity.ok(comment);
    }

    @GetMapping
    public ResponseEntity<List<CommentDto>> getByUserComment(@PathVariable Long userId) {
        log.info("Request received GET /user/{}/comments.", userId);
        List<CommentDto> comments = commentService.getAllUserComments(userId);
        log.info("Sending user comments list size {}.", comments.size());
        return ResponseEntity.ok(comments);
    }
}
