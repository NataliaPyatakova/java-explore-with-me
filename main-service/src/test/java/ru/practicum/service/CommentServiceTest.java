package ru.practicum.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentFullDto;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.enumeration.EventState;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommentServiceTest {

    @Autowired
    private CommentService commentService;
    private final EntityManager em;

    private static final long TIMEOUT = 5;
    private final LocalDateTime date = LocalDateTime.now().plusDays(1);
    private final LocalDateTime now = LocalDateTime.now();
    private final Category category = new Category().setName("test_category");
    private final Location location = new Location().setLat(15.15).setLon(16.16);
    private final User commentator = new User()
            .setEmail("commentator_email")
            .setName("commentator_name");
    private final User commentator2 = new User()
            .setEmail("commentator2_email")
            .setName("commentator2_name");
    private final User initiator = new User()
            .setEmail("email_initiator@")
            .setName("initiator_name");
    private final Event event = new Event()
            .setTitle("test_title")
            .setDescription("test_description")
            .setAnnotation("test_annotation")
            .setEventDate(date)
            .setPaid(true)
            .setParticipantLimit(0)
            .setRequestModeration(true)
            .setCreatedOn(now)
            .setState(EventState.PUBLISHED);
    private final Event event2 = new Event()
            .setTitle("test_title2")
            .setDescription("test_description2")
            .setAnnotation("test_annotation2")
            .setEventDate(date.plusHours(1))
            .setPaid(true)
            .setParticipantLimit(0)
            .setRequestModeration(true)
            .setCreatedOn(now)
            .setState(EventState.PUBLISHED);
    private final NewCommentDto newCommentDto = new NewCommentDto().setText("test_comment");
    private final UpdateCommentDto updateCommentDto = new UpdateCommentDto().setText("updated_test_comment");
    private final NewCommentDto newCommentDto2 = new NewCommentDto().setText("test_comment2");

    @Test
    @DisplayName("Сохранение comment - все хорошо")
    public void testSave() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto result = commentService.save(newCommentDto, commentator.getId(), event.getId());
        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getId());
        Assertions.assertEquals(commentator.getId(), result.getAuthor());
        Assertions.assertEquals(event.getId(), result.getEvent());
        Assertions.assertEquals(newCommentDto.getText(), result.getText());
        Assertions.assertNotNull(result.getDateCreation());
    }

    @Test
    @DisplayName("Сохранение comment - неправильный user")
    public void testSaveWrongUser() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        try {
            commentService.save(newCommentDto, 999L, event.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Сохранение comment - неправильное событие")
    public void testSaveWrongEvent() {
        em.persist(commentator);
        try {
            commentService.save(newCommentDto, commentator.getId(), 999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Event with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление comment - все хорошо")
    public void testUpdate() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        CommentFullDto result = commentService.update(updateCommentDto, commentFullDto.getId(), commentator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(commentFullDto.getId(), result.getId());
        Assertions.assertEquals(commentFullDto.getAuthor(), result.getAuthor());
        Assertions.assertEquals(commentFullDto.getEvent(), result.getEvent());
        Assertions.assertEquals(updateCommentDto.getText(), result.getText());
        Assertions.assertEquals(commentFullDto.getDateCreation(), result.getDateCreation());
    }

    @Test
    @DisplayName("Обновление comment - неверный комментарий")
    public void testUpdateWrongCommentId() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        commentService.save(newCommentDto, commentator.getId(), event.getId());
        try {
            commentService.update(updateCommentDto, 999L, commentator.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("Comment with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление comment - несуществующий user")
    public void testUpdateNoUserId() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        try {
            commentService.update(updateCommentDto, commentFullDto.getId(), 999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление comment - неверный user")
    public void testUpdateWrongUserId() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        try {
            commentService.update(updateCommentDto, commentFullDto.getId(), initiator.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Validation Error", e.getMessage());
            Assertions.assertEquals("Current user isn't an author of current comment", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск comment по id - все хорошо")
    public void testFindById() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        CommentFullDto result = commentService.findById(commentFullDto.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(commentFullDto.getId(), result.getId());
        Assertions.assertEquals(commentFullDto.getAuthor(), result.getAuthor());
        Assertions.assertEquals(commentFullDto.getEvent(), result.getEvent());
        Assertions.assertEquals(commentFullDto.getText(), result.getText());
        Assertions.assertEquals(commentFullDto.getDateCreation(), result.getDateCreation());
    }

    @Test
    @DisplayName("Поиск comment по id - нет такого коммента")
    public void testFindByIdWrongCommentId() {
        try {
            commentService.findById(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Comment with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Удаление comment по id - все хорошо")
    public void testDeleteById() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        commentService.deleteById(commentFullDto.getId());
        try {
            commentService.findById(commentFullDto.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("Comment with id=" + commentFullDto.getId() + " was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Удаление comment по id - неверный commentId")
    public void testDeleteByIdWrongCommentId() {
        try {
            commentService.deleteById(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Comment with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Удаление comment по id и userId - все хорошо")
    public void testDeleteByUserIdAndCommentId() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        commentService.deleteByUserIdAndCommentId(commentator.getId(), commentFullDto.getId());
        try {
            commentService.findById(commentFullDto.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("Comment with id=" + commentFullDto.getId() + " was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Удаление comment по id и userId - неверный commentId")
    public void testDeleteByUserIdAndCommentIdWrongCommentId() {
        em.persist(commentator);
        try {
            commentService.deleteByUserIdAndCommentId(commentator.getId(), 999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Comment with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Удаление comment по id и userId - неверный userId")
    public void testDeleteByUserIdAndCommentIdWrongUserId() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        try {
            commentService.deleteByUserIdAndCommentId(999L, commentFullDto.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Удаление comment по id и userId - неверный userId")
    public void testDeleteByUserIdAndCommentIdIncorrectUserId() {
        em.persist(commentator);
        em.persist(commentator2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        try {
            commentService.deleteByUserIdAndCommentId(commentator2.getId(), commentFullDto.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Validation Error", e.getMessage());
            Assertions.assertEquals("Current user isn't an author of current comment", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск comment по userId - все хорошо")
    public void testFindAllByUserId() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        List<CommentShortDto> result = commentService.findAllByUserId(commentator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(commentFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(commentFullDto.getEvent(), result.getFirst().getEvent());
        Assertions.assertEquals(commentFullDto.getText(), result.getFirst().getText());
        Assertions.assertEquals(commentFullDto.getDateCreation(), result.getFirst().getDateCreation());
    }

    @Test
    @DisplayName("Поиск comment по userId - нет комментариев")
    public void testFindAllByUserIdNoComments() {
        em.persist(commentator);
        List<CommentShortDto> result = commentService.findAllByUserId(commentator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск comment по userId - неверный пользователь")
    public void testFindAllByUserIdWrongUserId() {
        try {
            commentService.findAllByUserId(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск comment по userId - сортировка по дате комментария")
    public void testFindAllByUserIdSortedByDateCreation() throws InterruptedException {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event2.setInitiator(initiator);
        event2.setCategory(category);
        event2.setLocation(location);
        em.persist(event);
        em.persist(event2);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        //задержка в 5 сек, чтобы комментарии по времени разъехались
        TimeUnit.SECONDS.sleep(TIMEOUT);
        CommentFullDto commentFullDto2 = commentService.save(newCommentDto2, commentator.getId(), event2.getId());
        List<CommentShortDto> result = commentService.findAllByUserId(commentator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(commentFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(commentFullDto.getEvent(), result.getFirst().getEvent());
        Assertions.assertEquals(commentFullDto.getText(), result.getFirst().getText());
        Assertions.assertEquals(commentFullDto.getDateCreation(), result.getFirst().getDateCreation());
        Assertions.assertEquals(commentFullDto2.getId(), result.getLast().getId());
        Assertions.assertEquals(commentFullDto2.getEvent(), result.getLast().getEvent());
        Assertions.assertEquals(commentFullDto2.getText(), result.getLast().getText());
        Assertions.assertEquals(commentFullDto2.getDateCreation(), result.getLast().getDateCreation());
    }

    @Test
    @DisplayName("Поиск comment по eventId - все хорошо")
    public void testFindAllByEventId() {
        em.persist(commentator);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        List<CommentFullDto> result = commentService.findAllByEventId(event.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(commentFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(commentFullDto.getEvent(), result.getFirst().getEvent());
        Assertions.assertEquals(commentFullDto.getAuthor(), result.getFirst().getAuthor());
        Assertions.assertEquals(commentFullDto.getText(), result.getFirst().getText());
        Assertions.assertEquals(commentFullDto.getDateCreation(), result.getFirst().getDateCreation());
    }

    @Test
    @DisplayName("Поиск comment по eventId - нет комментариев")
    public void testFindAllByEventIdNoComments() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        List<CommentFullDto> result = commentService.findAllByEventId(event.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск comment по eventId - неверный eventId")
    public void testFindAllByEventIdWrongEventId() {
        try {
            commentService.findAllByEventId(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Event with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск comment по eventId - сортировка по дате комментария")
    public void testFindAllByEventISortedByDateCreation() throws InterruptedException {
        em.persist(commentator);
        em.persist(commentator2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        CommentFullDto commentFullDto = commentService.save(newCommentDto, commentator.getId(), event.getId());
        //задержка в 5 сек, чтобы комментарии по времени разъехались
        TimeUnit.SECONDS.sleep(TIMEOUT);
        CommentFullDto commentFullDto2 = commentService.save(newCommentDto2, commentator2.getId(), event.getId());
        List<CommentFullDto> result = commentService.findAllByEventId(event.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(commentFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(commentFullDto.getEvent(), result.getFirst().getEvent());
        Assertions.assertEquals(commentFullDto.getText(), result.getFirst().getText());
        Assertions.assertEquals(commentFullDto.getDateCreation(), result.getFirst().getDateCreation());
        Assertions.assertEquals(commentFullDto2.getId(), result.getLast().getId());
        Assertions.assertEquals(commentFullDto2.getEvent(), result.getLast().getEvent());
        Assertions.assertEquals(commentFullDto2.getText(), result.getLast().getText());
        Assertions.assertEquals(commentFullDto2.getDateCreation(), result.getLast().getDateCreation());
    }
}