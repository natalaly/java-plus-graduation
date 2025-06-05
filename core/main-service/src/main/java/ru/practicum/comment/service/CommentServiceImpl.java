package ru.practicum.comment.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.GetCommentsAdminRequest;
import ru.practicum.comment.model.Comment;
import ru.practicum.comment.repository.CommentRepository;
import ru.practicum.event.enums.State;
import ru.practicum.event.model.Event;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

@Transactional
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;

  /**
   * /users/{userId}/comments?eventId={eventId}
   * <p> Saves a new comment data initiated by a current user.
   */
  @Override
  public Comment addComment(final Comment comment, final Event event) {
    Long userId = comment.getUserId();

    if (!State.PUBLISHED.equals(event.getState())) {
      log.warn("Can not add comment to the event that is not published, event state = {}",
          event.getState());
      throw new ConflictException("Cannot save comments for not published event.");
    }

    comment.setCreated(LocalDateTime.now());

    if (userId.equals(event.getInitiatorId())) {
      comment.setInitiator(true);
    }
    return commentRepository.save(comment);
  }

  /**
   * /users/{userId}/comments/{commentId}
   * <p> Deletes current user's comment
   */
  @Override
  public void delete(final Long userId, final Long commentId) {
    Comment comment = fetchComment(commentId);

    if (!comment.getUserId().equals(userId)) {
      throw new ConflictException("User can delete only their own comments.");
    }

    commentRepository.delete(comment);
  }

  /**
   * /admin/comments/{commentId}
   * <p> Deletes specified comment
   */
  @Override
  public void delete(final Long commentId) {
    Comment comment = fetchComment(commentId);
    commentRepository.delete(comment);
  }

  /**
   * /users/{userId}/comments/{commentId}
   * <p> Update current user's comment
   */
  @Override
  public Comment updateUserComment(final Long userId, final Long commentId,
                                      final CommentDto commentData) {
    Comment comment = fetchComment(commentId);
    if (!comment.getUserId().equals(userId)) {
      throw new ConflictException("User can update only their own comments.");
    }
    comment.setContent(commentData.getContent());
    return commentRepository.save(comment);
  }

  /**
   * /users/{userId}/comments
   * <p> Get all comments created by a given user, used for Private API
   */
  @Transactional(readOnly = true)
  @Override
  public List<Comment> getAllUserComments(final Long userId) {
    return commentRepository.findByUserId(userId);
  }

  /**
   * /admin/comment
   * <p> Get comments related specified event, used for Admin API
   */
  @Transactional(readOnly = true)
  @Override
  public List<Comment> getAllEventComments(final GetCommentsAdminRequest param) {
    return getEventComments(param.getEventId(), param.getFrom(), param.getSize());
  }

  /**
   * /events/{eventId}/comments
   * <p> Get comments of given event for public API
   */
  @Transactional(readOnly = true)
  @Override
  public List<Comment> getAllEventComments(final Long eventId, final int from, final int size) {
    return getEventComments(eventId,from,size);
  }

  private List<Comment> getEventComments(final Long eventId, final int from, final int size) {
    final PageRequest page = PageRequest.of(from / size, size);
    return commentRepository.findAllByEventId(eventId, page).getContent();
  }

  private Comment fetchComment(final Long commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> {
          log.warn("Comment with ID {} not found.", commentId);
          return new NotFoundException("The comment not found.");
        });
  }
}
