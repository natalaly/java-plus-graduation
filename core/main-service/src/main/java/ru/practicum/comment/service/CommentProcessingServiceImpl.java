package ru.practicum.comment.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.api.UserOperations;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.GetCommentsAdminRequest;
import ru.practicum.comment.mapper.CommentMapper;
import ru.practicum.comment.model.Comment;
import ru.practicum.dto.UserShortDto;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.NotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentProcessingServiceImpl implements CommentProcessingService {

  private final CommentService commentService;
  private final UserOperations userClient;
  private final EventRepository eventRepository;

  @Override
  public CommentDto addComment(final CommentDto commentDto) {
    UserShortDto user = fetchUser(commentDto.getUserId());
    Event event = fetchEvent(commentDto.getEventId());
    // TODO make better
    Comment comment = commentService.addComment(CommentMapper.mapTo(commentDto, user, event),
        event);
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
    fetchUser(userId);
    Comment updatedComment = commentService.updateUserComment(userId, commentId, commentDto);
    return CommentMapper.mapToCommentDto(updatedComment);
  }

  @Override
  public List<CommentDto> getAllUserComments(Long userId) {
    fetchUser(userId);
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

  private UserShortDto fetchUser(final Long userId) {
    log.debug("Sending request to userClient to get user with ID {}", userId);
    UserShortDto user = userClient.getUser(userId);
    log.debug("Got user response: {}", user.getId());
    return user;
  }

  private Event fetchEvent(final Long eventId) {
    return eventRepository.findById(eventId)
        .orElseThrow(() -> {
          log.warn("Event with ID {} not found.", eventId);
          return new NotFoundException("The event not found.");
        });
  }

  private void validateEventExists(final Long eventId) {
    log.debug("Validating event with ID {} is not null and exist.", eventId);
    if (eventId == null || !userClient.existsById(eventId)) {
      log.warn("Validation Event with ID = {} is not null and existed failed.", eventId);
      throw new NotFoundException("Event not found.");
    }
    log.debug("Success: Event ID {} is valid.", eventId);
  }

}
