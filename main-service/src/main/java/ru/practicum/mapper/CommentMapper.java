package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.comment.*;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static Comment mapToCommentForCreate(NewCommentDto newCommentDto, ParamCommentSave param) {
        Comment comment = new Comment();
        comment.setAuthor(param.getAuthor());
        comment.setEvent(param.getEvent());
        comment.setText(newCommentDto.getText());
        comment.setDateCreation(LocalDateTime.now());
        return comment;
    }

    public static Comment mapToCommentForUpdate(UpdateCommentDto updateDto, Comment foundedComment) {
        Comment comment = new Comment();
        comment.setId(foundedComment.getId());
        comment.setAuthor(foundedComment.getAuthor());
        comment.setEvent(foundedComment.getEvent());
        comment.setDateCreation(foundedComment.getDateCreation());
        if (updateDto.getText() != null) {
            comment.setText(updateDto.getText());
        } else {
            comment.setText(foundedComment.getText());
        }
        return comment;
    }

    public static CommentFullDto mapToCommentFullDto(Comment comment) {
        CommentFullDto commentFullDto = new CommentFullDto();
        commentFullDto.setId(comment.getId());
        commentFullDto.setAuthor(comment.getAuthor().getId());
        commentFullDto.setEvent(comment.getEvent().getId());
        commentFullDto.setText(comment.getText());
        commentFullDto.setDateCreation(comment.getDateCreation());
        return commentFullDto;
    }

    public static CommentShortDto mapToCommentShortDto(Comment comment) {
        CommentShortDto commentShortDto = new CommentShortDto();
        commentShortDto.setId(comment.getId());
        commentShortDto.setEvent(comment.getEvent().getId());
        commentShortDto.setText(comment.getText());
        commentShortDto.setDateCreation(comment.getDateCreation());
        return commentShortDto;
    }
}