package ru.practicum.comment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.comment.client.event.EventClient;
import ru.practicum.comment.client.user.UserClient;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.GetCommentsAdminRequest;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.dto.EventFullDto;
import ru.practicum.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentProcessingServiceImpl implements CommentProcessingService {

  private final CommentService commentService;
  private final UserClient userClient;
  private final EventClient eventClient;

  @Override
  public CommentDto addComment(final CommentDto commentDto) {
    log.debug("Saving a new comment: {}", commentDto);
    validateUserExists(commentDto.getUserId());
    EventFullDto event = fetchEvent(commentDto.getEventId());
    Comment commentToSave = CommentMapper.mapTo(commentDto, commentDto.getUserId(), event.getId());
    Comment comment = commentService.addComment(commentToSave, event);
    return CommentMapper.mapToCommentDto(comment);
  }

  @Override
  public void delete(final Long userId, final Long commentId) {
    commentService.delete(userId, commentId);
  }

  @Override
  public void delete(Long commentId) {
    commentService.delete(commentId);
  }

  @Override
  public CommentDto updateUserComment(final Long userId, final Long commentId,
                                      final CommentDto commentDto) {
    validateUserExists(userId);
    Comment updatedComment = commentService.updateUserComment(userId, commentId, commentDto);
    return CommentMapper.mapToCommentDto(updatedComment);
  }

  @Override
  public List<CommentDto> getAllUserComments(Long userId) {
    validateUserExists(userId);
    List<Comment> comments = commentService.getAllUserComments(userId);
    return CommentMapper.mapToCommentDto(comments);
  }

  @Override
  public List<CommentDto> getAllEventComments(GetCommentsAdminRequest param) {
    return CommentMapper.mapToCommentDto(commentService.getAllEventComments(param));
  }

  @Override
  public List<CommentDto> getAllEventComments(Long eventId, int from, int size) {
    validateEventExists(eventId);
    return CommentMapper.mapToCommentDto(commentService.getAllEventComments(eventId, from, size));
  }

  private void validateUserExists(final Long userId) {
    log.debug("Validating user with ID {} is not null and exist.", userId);
    if (userId == null || !userClient.existsById(userId)) {
      log.warn("Validation User with ID = {} is not null and existed failed.", userId);
      throw new NotFoundException("User not found.");
    }
    log.debug("Success: User ID {} is valid.", userId);
  }

  private EventFullDto fetchEvent(final Long eventId) {
    log.debug("Sending request to eventClient to get event with ID {}", eventId);
    EventFullDto event = eventClient.getEvent(eventId);
    log.debug("Got eventClient response: {}", event.getId());
    return event;
  }

  private void validateEventExists(final Long eventId) {
    log.debug("Validating event with ID {} is not null and exist.", eventId);
    if (eventId == null || !eventClient.existsById(eventId)) {
      log.warn("Validation Event with ID = {} is not null and existed failed.", eventId);
      throw new NotFoundException("Event not found.");
    }
    log.debug("Success: Event ID {} is valid.", eventId);
  }

}
