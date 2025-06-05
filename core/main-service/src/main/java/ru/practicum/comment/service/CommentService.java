package ru.practicum.comment.service;

import ru.practicum.comment.dto.GetCommentsAdminRequest;
import ru.practicum.comment.dto.CommentDto;

import java.util.List;
import ru.practicum.comment.model.Comment;
import ru.practicum.event.model.Event;

public interface CommentService {

  Comment addComment(Comment commentDto, Event event);

  void delete(Long userId, Long commentId);

  void delete(Long commentId);

  Comment updateUserComment(Long userId, Long commentId, CommentDto commentDto);

  List<Comment> getAllUserComments(Long userId);

  List<Comment> getAllEventComments(GetCommentsAdminRequest param);

  List<Comment> getAllEventComments(Long eventId, int from, int size);

}
