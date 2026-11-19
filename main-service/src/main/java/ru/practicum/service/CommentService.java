package ru.practicum.service;

import ru.practicum.dto.comment.CommentFullDto;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentFullDto save(NewCommentDto newCommentDto, Long userId, Long eventId);

    CommentFullDto update(UpdateCommentDto updateCommentDto, Long commentId, Long userId);

    CommentFullDto findById(Long commentId);

    void deleteById(Long commentId);

    void deleteByUserIdAndCommentId(Long userId, Long commentId);

    List<CommentShortDto> findAllByUserId(Long userId);

    List<CommentFullDto> findAllByEventId(Long eventId);

}
