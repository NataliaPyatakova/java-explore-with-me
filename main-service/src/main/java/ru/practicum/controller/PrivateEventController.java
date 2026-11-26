package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentFullDto;
import ru.practicum.dto.comment.CommentShortDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.CommentService;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}")
@Slf4j
@RequiredArgsConstructor
@Validated
public class PrivateEventController {

    private final EventService eventService;
    private final CommentService commentService;

    @GetMapping("/events")
    public List<EventShortDto> findEventsByUserId(@PathVariable Long userId,
                                                  @RequestParam(value = "from", defaultValue = "0") Integer from,
                                                  @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("PrivateEventController findEventsByUserId from {} size {} userId {}", from, size, userId);
        ParamEventFindByUser params = new ParamEventFindByUser(userId, from, size);
        return eventService.findEventsByUserId(params);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto save(@PathVariable Long userId,
                             @RequestBody @Valid NewEventDto newEventDto) {
        log.info("PrivateEventController save event {} userId {}", newEventDto, userId);
        return eventService.save(newEventDto, userId);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto findEventByIdAndByUserId(@PathVariable Long userId,
                                                 @PathVariable Long eventId) {
        log.info("PrivateEventController findEventByIdAndByUserId userId {} eventId {}", userId, eventId);
        return eventService.findEventByIdAndByUserId(eventId, userId);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateByInitiator(@PathVariable Long userId,
                                          @PathVariable Long eventId,
                                          @RequestBody @Valid UpdateEventUserRequest updateDto) {
        log.info("PrivateEventController updateByInitiator userId {} eventId {} updateDto {}", userId, eventId, updateDto);
        return eventService.updateByInitiator(updateDto, eventId, userId);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> findRequestsByEventId(@PathVariable Long userId,
                                                               @PathVariable Long eventId) {
        log.info("PrivateEventController findRequestsByEventId userId {} eventId {}", userId, eventId);
        return eventService.findRequestsByEventId(eventId, userId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestsStatus(@PathVariable Long userId,
                                                               @PathVariable Long eventId,
                                                               @RequestBody @Valid EventRequestStatusUpdateRequest dto) {
        log.info("PrivateEventController updateRequestsStatus userId {} eventId {} dto {}", userId, eventId, dto);
        return eventService.updateRequestsStatus(dto, eventId, userId);
    }

    @PostMapping("/events/{eventId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentFullDto saveComment(@PathVariable Long userId,
                                      @PathVariable Long eventId,
                                      @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("PrivateEventController saveComment newCommentDto {}, event {}, userId {}", newCommentDto, eventId, userId);
        return commentService.save(newCommentDto, userId, eventId);
    }

    @PatchMapping("/commentaries/{commentId}")
    public CommentFullDto updateComment(@PathVariable Long userId,
                                        @PathVariable Long commentId,
                                        @RequestBody @Valid UpdateCommentDto updateCommentDto) {
        log.info("PrivateEventController updateComment updateCommentDto {}, commentId {}, userId {}",
                updateCommentDto, commentId, userId);
        return commentService.update(updateCommentDto, commentId, userId);
    }

    @GetMapping("/commentaries/{commentId}")
    public CommentFullDto findCommentById(@PathVariable Long userId,
                                          @PathVariable Long commentId) {
        log.info("PrivateEventController findCommentById userId {}, commentId {}", userId, commentId);
        return commentService.findById(commentId);
    }

    @DeleteMapping("/commentaries/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("PrivateEventController delete commentId {}, userId {}", commentId, userId);
        commentService.deleteByUserIdAndCommentId(userId, commentId);
    }

    @GetMapping("/commentaries")
    public List<CommentShortDto> findAllCommentariesByUserId(@PathVariable Long userId) {
        log.info("PrivateEventController findAllCommentariesByUserId userId {}", userId);
        return commentService.findAllByUserId(userId);
    }
}
