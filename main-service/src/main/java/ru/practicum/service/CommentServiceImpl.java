package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.*;
import ru.practicum.enumeration.EventState;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentFullDto save(NewCommentDto newCommentDto, Long userId, Long eventId) {
        log.info("save userId {}, newCommentDto {}", userId, newCommentDto);
        User user = UserMapper.mapToUserFromUserDto(userService.findById(userId));
        //событие должно быть опубликовано
        Event event = eventRepository.findEventByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found",
                        "The required object was not found."));
        ParamCommentSave paramCommentSave = new ParamCommentSave(user, event);
        Comment comment = commentRepository.save(CommentMapper.mapToCommentForCreate(newCommentDto, paramCommentSave));
        return CommentMapper.mapToCommentFullDto(comment);
    }

    @Override
    @Transactional
    public CommentFullDto update(UpdateCommentDto updateCommentDto, Long commentId, Long userId) {
        log.info("update commentId {}, userId {}, updateCommentDto {}", commentId, userId, updateCommentDto);
        Comment foundedComment = findCommentById(commentId);
        userService.findById(userId);
        if (!Objects.equals(foundedComment.getAuthor().getId(), userId)) {
            throw new ValidationException("Validation Error",
                    "Current user isn't an author of current comment");
        }
        Comment comment = commentRepository.save(CommentMapper.mapToCommentForUpdate(updateCommentDto, foundedComment));
        return CommentMapper.mapToCommentFullDto(comment);
    }

    @Override
    public CommentFullDto findById(Long commentId) {
        log.info("findById commentId {}", commentId);
        Comment comment = findCommentById(commentId);
        return CommentMapper.mapToCommentFullDto(comment);
    }

    @Override
    @Transactional
    public void deleteById(Long commentId) {
        log.info("deleteById commentId {}", commentId);
        findCommentById(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndCommentId(Long userId, Long commentId) {
        log.info("deleteByUserIdAndCommentId commentId {}", commentId);
        userService.findById(userId);
        Comment foundedComment = findCommentById(commentId);
        if (!Objects.equals(foundedComment.getAuthor().getId(), userId)) {
            throw new ValidationException("Validation Error",
                    "Current user isn't an author of current comment");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentShortDto> findAllByUserId(Long userId) {
        log.info("findAllByUserId userId {}", userId);
        userService.findById(userId);
        List<Comment> listComments = commentRepository.findAllByAuthorId(userId);
        return listComments.stream()
                .map(CommentMapper::mapToCommentShortDto)
                .sorted(Comparator.comparing(CommentShortDto::getDateCreation))
                .toList();
    }

    @Override
    public List<CommentFullDto> findAllByEventId(Long eventId) {
        log.info("findAllByEventId eventId {}", eventId);
        //событие должно быть опубликовано
        eventRepository.findEventByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found",
                        "The required object was not found."));
        List<Comment> listComments = commentRepository.findAllByEventId(eventId);
        return listComments.stream()
                .map(CommentMapper::mapToCommentFullDto)
                .sorted(Comparator.comparing(CommentFullDto::getDateCreation))
                .toList();
    }

    private Comment findCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + id + " was not found",
                        "The required object was not found."));
    }
}