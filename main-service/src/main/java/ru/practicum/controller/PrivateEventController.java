package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}")
@Slf4j
@RequiredArgsConstructor
@Validated
public class PrivateEventController {

    private final EventService eventService;

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
}
