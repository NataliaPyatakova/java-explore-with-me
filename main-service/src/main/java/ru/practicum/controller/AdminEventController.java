package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.ParamEventsFindByAdmin;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.enumeration.EventState;
import ru.practicum.exception.BadRequestException;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class AdminEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> findEventsByAdmin(@RequestParam(value = "users", required = false) List<Long> users,
                                                @RequestParam(value = "states", required = false) List<String> states,
                                                @RequestParam(value = "categories", required = false) List<Long> categories,
                                                @RequestParam(value = "rangeStart", required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                @RequestParam(value = "rangeEnd", required = false)
                                                @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                @RequestParam(value = "from", defaultValue = "0") Integer from,
                                                @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("AdminEventController findEventsByAdmin users {} states {} categories {} rangeStart {} rangeEnd {} from {} size {}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        ParamEventsFindByAdmin params = new ParamEventsFindByAdmin()
                .setUsers(users)
                .setCategories(categories)
                .setRangeStart(rangeStart)
                .setRangeEnd(rangeEnd)
                .setFrom(from)
                .setSize(size);
        List<EventState> listEventState = new ArrayList<>();
        if (states != null) {
            states.forEach(state -> {
                        try {
                            listEventState.add(EventState.valueOf(state));
                        } catch (IllegalArgumentException e) {
                            throw new BadRequestException("Bad request", "Parameter state could be PENDING or PUBLISHED or CANCELED");
                        }
                    }
            );
            params.setStates(listEventState);
        }
        return eventService.findEventsByAdmin(params);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateByAdmin(@PathVariable Long eventId,
                                      @RequestBody @Valid UpdateEventAdminRequest updateDto) {
        log.info("AdminEventController updateByAdmin eventId {} updateDto {}", eventId, updateDto);
        return eventService.updateByAdmin(updateDto, eventId);
    }
}
