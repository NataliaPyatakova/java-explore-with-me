package ru.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.StatClient;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.ParamEventsFindByParam;
import ru.practicum.enumeration.Sort;
import ru.practicum.exception.BadRequestException;
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
    private final StatClient statClient;

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
                                                          @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                          HttpServletRequest request) {
        log.info("PublicEventController findPublishedEventsByParam text {}, categories {}, paid {}, rangeStart {}, " +
                        "rangeEnd {}, onlyAvailable {}, sort {}, from {}, size {}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        ParamEventsFindByParam params = new ParamEventsFindByParam()
                .setText(text)
                .setCategories(categories)
                .setPaid(paid)
                .setRangeStart(rangeStart)
                .setRangeEnd(rangeEnd)
                .setOnlyAvailable(onlyAvailable)
                .setFrom(from)
                .setSize(size);
        if (sort != null) {
            try {
                params.setSort(Sort.valueOf(sort));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Bad request", "Parameter sort could be EVENT_DATE or VIEWS");
            }
        }
        List<EventShortDto> result = eventService.findPublishedEventsByParam(params);
        EndpointHit endpointHit = new EndpointHit()
                .setApp("ExplorerWithMeApplication")
                .setUri(request.getRequestURI())
                .setIp(request.getRemoteAddr())
                .setTimestamp(LocalDateTime.now());
        statClient.hit(endpointHit);
        return result;
    }

    @GetMapping("/{id}")
    public EventFullDto findPublishedEventById(@PathVariable Long id, HttpServletRequest request) {
        log.info("PublicEventController findPublishedEventById id {}", id);
        EventFullDto result = eventService.findPublishedEventById(id);
        EndpointHit endpointHit = new EndpointHit()
                .setApp("ExplorerWithMeApplication")
                .setUri(request.getRequestURI())
                .setIp(request.getRemoteAddr())
                .setTimestamp(LocalDateTime.now());
        statClient.hit(endpointHit);
        return result;
    }
}
