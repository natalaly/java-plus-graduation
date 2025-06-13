package ru.practicum.comment.service;

import java.util.List;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.GetCommentsAdminRequest;
import ru.practicum.comment.model.Comment;
import ru.practicum.dto.EventFullDto;

public interface CommentService {

  Comment addComment(Comment comment, EventFullDto event);

  void delete(Long userId, Long commentId);

  void delete(Long commentId);

  Comment updateUserComment(Long userId, Long commentId, CommentDto commentDto);

  List<Comment> getAllUserComments(Long userId);

  List<Comment> getAllEventComments(GetCommentsAdminRequest param);

  List<Comment> getAllEventComments(Long eventId, int from, int size);

}
