package ru.practicum.comment.service;

import java.util.List;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.dto.GetCommentsAdminRequest;

public interface CommentProcessingService {

  CommentDto addComment(CommentDto commentDto);

  void delete(Long userId, Long commentId);

  void delete(Long commentId);

  CommentDto updateUserComment(Long userId, Long commentId, CommentDto commentDto);

  List<CommentDto> getAllUserComments(Long userId);

  List<CommentDto> getAllEventComments(GetCommentsAdminRequest param);

  List<CommentDto> getAllEventComments(Long eventId, int from, int size);

}
