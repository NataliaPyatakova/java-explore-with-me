package ru.practicum.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.client.StatClient;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.LocationDto;
import ru.practicum.dto.event.ParamEventsFindByParam;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enumeration.EventState;
import ru.practicum.enumeration.Sort;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicEventController.class)
public class PublicEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private StatClient statClient;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LocalDateTime date = LocalDateTime.now();
    private final LocationDto locationDto = new LocationDto().setLon(123.456).setLat(345.678);
    private final UserShortDto initiatorShort = new UserShortDto().setId(1L).setName("test_name");
    private final CategoryDto categoryDto = new CategoryDto().setId(1L).setName("test_category");
    private final EventFullDto eventFullDto = new EventFullDto()
            .setId(3L)
            .setTitle("event")
            .setAnnotation("event_annotation")
            .setDescription("event_description")
            .setEventDate(date)
            .setPaid(true)
            .setRequestModeration(true)
            .setParticipantLimit(0)
            .setLocation(locationDto)
            .setInitiator(initiatorShort)
            .setCategory(categoryDto)
            .setCreatedOn(date)
            .setPublishedOn(date)
            .setConfirmedRequests(0L)
            .setViews(0L)
            .setState(EventState.PUBLISHED);
    List<EventShortDto> list = new ArrayList<>();
    private final EventShortDto eventShortDto = new EventShortDto()
            .setId(3L)
            .setTitle("event")
            .setAnnotation("event_annotation")
            .setEventDate(date)
            .setPaid(true)
            .setInitiator(initiatorShort)
            .setCategory(categoryDto)
            .setConfirmedRequests(0L)
            .setViews(0L);

    @Test
    @DisplayName("Get event - все хорошо")
    void testFindPublishedEventByIdAllOk() throws Exception {
        when(eventService.findPublishedEventById(eventFullDto.getId())).thenReturn(eventFullDto);
        mockMvc.perform(get("/events/" + eventFullDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(eventFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(eventFullDto.getTitle()), String.class))
                .andExpect(jsonPath("$.annotation", is(eventFullDto.getAnnotation()), String.class))
                .andExpect(jsonPath("$.description", is(eventFullDto.getDescription()), String.class))
                .andExpect(jsonPath("$.eventDate", is(eventFullDto.getEventDate().format(dtf))))
                .andExpect(jsonPath("$.category.id", is(eventFullDto.getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.category.name", is(eventFullDto.getCategory().getName()), String.class))
                .andExpect(jsonPath("$.confirmedRequests", is(eventFullDto.getConfirmedRequests()), Long.class))
                .andExpect(jsonPath("$.createdOn", is(eventFullDto.getCreatedOn().format(dtf))))
                .andExpect(jsonPath("$.initiator.id", is(eventFullDto.getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.initiator.name", is(eventFullDto.getInitiator().getName()), String.class))
                .andExpect(jsonPath("$.location.lat", is(eventFullDto.getLocation().getLat()), Double.class))
                .andExpect(jsonPath("$.location.lon", is(eventFullDto.getLocation().getLon()), Double.class))
                .andExpect(jsonPath("$.paid", is(eventFullDto.getPaid()), Boolean.class))
                .andExpect(jsonPath("$.participantLimit", is(eventFullDto.getParticipantLimit()), Integer.class))
                .andExpect(jsonPath("$.publishedOn", is(eventFullDto.getPublishedOn().format(dtf))))
                .andExpect(jsonPath("$.requestModeration", is(eventFullDto.getRequestModeration()), Boolean.class))
                .andExpect(jsonPath("$.state", is(eventFullDto.getState().toString()), String.class))
                .andExpect(jsonPath("$.views", is(eventFullDto.getViews()), Long.class));
    }

    @Test
    @DisplayName("Get all events by param  - все параметры")
    void testFindPublishedEventsByParamAllOk() throws Exception {
        list.add(eventShortDto);
        List<Long> categories = new ArrayList<>();
        categories.add(1L);
        ParamEventsFindByParam params = new ParamEventsFindByParam()
                .setText("event")
                .setCategories(categories)
                .setPaid(true)
                .setRangeStart(date)
                .setRangeEnd(date)
                .setOnlyAvailable(true)
                .setSort(Sort.EVENT_DATE)
                .setFrom(1)
                .setSize(2);
        when(eventService.findPublishedEventsByParam(params)).thenReturn(list);
        mockMvc.perform(get("/events")
                        .param("text", "event")
                        .param("categories", "1")
                        .param("paid", "true")
                        .param("rangeStart", String.valueOf(date))
                        .param("rangeEnd", String.valueOf(date))
                        .param("onlyAvailable", "true")
                        .param("sort", "EVENT_DATE")
                        .param("from", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(eventShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].title", is(eventShortDto.getTitle()), String.class))
                .andExpect(jsonPath("$.[0].annotation", is(eventShortDto.getAnnotation()), String.class))
                .andExpect(jsonPath("$.[0].eventDate", is(eventShortDto.getEventDate().format(dtf))))
                .andExpect(jsonPath("$.[0].category.id", is(eventShortDto.getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.[0].category.name", is(eventShortDto.getCategory().getName()), String.class))
                .andExpect(jsonPath("$.[0].confirmedRequests", is(eventShortDto.getConfirmedRequests()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.id", is(eventShortDto.getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.name", is(eventShortDto.getInitiator().getName()), String.class))
                .andExpect(jsonPath("$.[0].paid", is(eventShortDto.getPaid()), Boolean.class))
                .andExpect(jsonPath("$.[0].views", is(eventShortDto.getViews()), Long.class));
    }

    @Test
    @DisplayName("Get all events by param  - задан только text")
    void testFindPublishedEventsByParamOnlyText() throws Exception {
        list.add(eventShortDto);
        ParamEventsFindByParam params = new ParamEventsFindByParam()
                .setText("event")
                //это дефолтные значения - сравнивать будут с ними в моке
                .setOnlyAvailable(false)
                .setFrom(0)
                .setSize(10);
        when(eventService.findPublishedEventsByParam(params)).thenReturn(list);
        mockMvc.perform(get("/events")
                        .param("text", "event"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(eventShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].title", is(eventShortDto.getTitle()), String.class))
                .andExpect(jsonPath("$.[0].annotation", is(eventShortDto.getAnnotation()), String.class))
                .andExpect(jsonPath("$.[0].eventDate", is(eventShortDto.getEventDate().format(dtf))))
                .andExpect(jsonPath("$.[0].category.id", is(eventShortDto.getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.[0].category.name", is(eventShortDto.getCategory().getName()), String.class))
                .andExpect(jsonPath("$.[0].confirmedRequests", is(eventShortDto.getConfirmedRequests()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.id", is(eventShortDto.getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.name", is(eventShortDto.getInitiator().getName()), String.class))
                .andExpect(jsonPath("$.[0].paid", is(eventShortDto.getPaid()), Boolean.class))
                .andExpect(jsonPath("$.[0].views", is(eventShortDto.getViews()), Long.class));
    }

    @Test
    @DisplayName("Get all events by param  - некорректно задан sort")
    void testFindPublishedEventsByParamWrongSort() throws Exception {
        list.add(eventShortDto);
        mockMvc.perform(get("/events")
                        .param("sort", "event"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get all events by param  - без параметров")
    void testFindPublishedEventsByParamNoParam() throws Exception {
        list.add(eventShortDto);
        //это дефолтные значения - сравнивать будут с ними в моке
        ParamEventsFindByParam params = new ParamEventsFindByParam()
                .setOnlyAvailable(false)
                .setFrom(0)
                .setSize(10);
        System.out.println(params);
        when(eventService.findPublishedEventsByParam(params)).thenReturn(list);
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(eventShortDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].title", is(eventShortDto.getTitle()), String.class))
                .andExpect(jsonPath("$.[0].annotation", is(eventShortDto.getAnnotation()), String.class))
                .andExpect(jsonPath("$.[0].eventDate", is(eventShortDto.getEventDate().format(dtf))))
                .andExpect(jsonPath("$.[0].category.id", is(eventShortDto.getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.[0].category.name", is(eventShortDto.getCategory().getName()), String.class))
                .andExpect(jsonPath("$.[0].confirmedRequests", is(eventShortDto.getConfirmedRequests()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.id", is(eventShortDto.getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.name", is(eventShortDto.getInitiator().getName()), String.class))
                .andExpect(jsonPath("$.[0].paid", is(eventShortDto.getPaid()), Boolean.class))
                .andExpect(jsonPath("$.[0].views", is(eventShortDto.getViews()), Long.class));
    }
}
