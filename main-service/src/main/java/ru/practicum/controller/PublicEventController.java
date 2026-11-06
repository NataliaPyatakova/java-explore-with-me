package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.ParamEventsFindByParam;
import ru.practicum.enumeration.Sort;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping(path = "/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> findPublishedEventsByParam(@RequestParam(value = "text", required = false) String text,
                                                          @RequestParam(value = "categories", required = false) List<Long> categories,
                                                          @RequestParam(value = "paid", required = false) Boolean paid,
                                                          @RequestParam(value = "rangeStart", required = false)
                                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                          @RequestParam(value = "rangeEnd", required = false)
                                                          @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                          @RequestParam(value = "onlyAvailable", defaultValue = "false") Boolean onlyAvailable,
                                                          @RequestParam(value = "sort", required = false) String sort,
                                                          @RequestParam(value = "from", defaultValue = "0") Integer from,
                                                          @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("PublicEventController findPublishedEventsByParam text {} categories {} paid {} rangeStart {} rangeEnd {} " +
                        "onlyAvailable {} sort {} from {} size {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        ParamEventsFindByParam params = new ParamEventsFindByParam()
                .setText(text)
                .setCategories(categories)
                .setRangeStart(rangeStart)
                .setRangeEnd(rangeEnd)
                .setOnlyAvailable(onlyAvailable)
                .setSort(Sort.valueOf(sort))
                .setFrom(from)
                .setSize(size);
        return eventService.findPublishedEventsByParam(params);
    }

    @GetMapping("/{id}")
    public EventFullDto findPublishedEventById(@PathVariable Long id) {
        log.info("PublicEventController findPublishedEventById id {}", id);
        return eventService.findPublishedEventById(id);
    }
}
