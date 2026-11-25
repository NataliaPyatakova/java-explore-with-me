package ru.practicum.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.dto.comment.*;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;

import java.time.LocalDateTime;

@SpringBootTest
public class CommentMapperTest {

    private final LocalDateTime date = LocalDateTime.now();
    private final Event event = new Event()
            .setId(1L)
            .setTitle("test_title")
            .setParticipantLimit(1)
            .setRequestModeration(true);
    private final User user = new User()
            .setId(1L)
            .setEmail("test_email")
            .setName("test_name");
    private final NewCommentDto newCommentDto = new NewCommentDto()
            .setText("text_comment");
    private final ParamCommentSave param = new ParamCommentSave(user, event);
    private final UpdateCommentDto updateDto = new UpdateCommentDto().setText("updated_text_comment");
    private final Comment foundedComment = new Comment()
            .setId(1L)
            .setAuthor(user)
            .setEvent(event)
            .setText("test_comment")
            .setDateCreation(date);

    @Test
    @DisplayName("Тест маппера из NewCommentDto в Comment")
    public void testMapToCommentForCreate() {
        Comment result = CommentMapper.mapToCommentForCreate(newCommentDto, param);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newCommentDto.getText(), result.getText());
        Assertions.assertEquals(param.getAuthor(), result.getAuthor());
        Assertions.assertEquals(param.getEvent(), result.getEvent());
        Assertions.assertNotNull(result.getDateCreation());
    }

    @Test
    @DisplayName("Тест маппера из UpdateCommentDto в Comment - не пустой текст")
    public void testMapToCommentForUpdateWithText() {
        Comment result = CommentMapper.mapToCommentForUpdate(updateDto, foundedComment);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(updateDto.getText(), result.getText());
        Assertions.assertEquals(foundedComment.getAuthor(), result.getAuthor());
        Assertions.assertEquals(foundedComment.getEvent(), result.getEvent());
        Assertions.assertEquals(foundedComment.getDateCreation(), result.getDateCreation());
    }

    @Test
    @DisplayName("Тест маппера из UpdateCommentDto в Comment - пустой текст")
    public void testMapToCommentForUpdateNoText() {
        updateDto.setText(null);
        Comment result = CommentMapper.mapToCommentForUpdate(updateDto, foundedComment);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedComment.getText(), result.getText());
        Assertions.assertEquals(foundedComment.getAuthor(), result.getAuthor());
        Assertions.assertEquals(foundedComment.getEvent(), result.getEvent());
        Assertions.assertEquals(foundedComment.getDateCreation(), result.getDateCreation());
    }

    @Test
    @DisplayName("Тест маппера из Comment в CommentFullDto")
    public void testMapToCommentFullDto() {
        CommentFullDto result = CommentMapper.mapToCommentFullDto(foundedComment);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedComment.getId(), result.getId());
        Assertions.assertEquals(foundedComment.getText(), result.getText());
        Assertions.assertEquals(foundedComment.getAuthor().getId(), result.getAuthor().getId());
        Assertions.assertEquals(foundedComment.getEvent().getId(), result.getEvent());
        Assertions.assertEquals(foundedComment.getDateCreation(), result.getDateCreation());
    }

    @Test
    @DisplayName("Тест маппера из Comment в CommentShortDto")
    public void testMapToCommentShortDto() {
        CommentShortDto result = CommentMapper.mapToCommentShortDto(foundedComment);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedComment.getId(), result.getId());
        Assertions.assertEquals(foundedComment.getText(), result.getText());
        Assertions.assertEquals(foundedComment.getEvent().getId(), result.getEvent());
        Assertions.assertEquals(foundedComment.getDateCreation(), result.getDateCreation());
    }
}