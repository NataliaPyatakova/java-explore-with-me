package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.LocationDto;
import ru.practicum.dto.event.ParamEventsFindByAdmin;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enumeration.EventState;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminEventController.class)
public class AdminEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EventService eventService;

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
            .setState(EventState.PENDING);
    private final UpdateEventAdminRequest updateEventAdminRequest = (UpdateEventAdminRequest) new UpdateEventAdminRequest()
            .setTitle("UpdateEventAdminRequest")
            .setDescription("UpdateEventAdminRequest_description")
            .setAnnotation("UpdateEventAdminRequest_annotation")
            .setEventDate(date.plusDays(1))
            .setCategory(2L)
            .setParticipantLimit(1)
            .setPaid(false)
            .setRequestModeration(false)
            .setLocationDto(locationDto);
    private final List<EventFullDto> list = new ArrayList<>();

    @Test
    @DisplayName("Patch event - все хорошо")
    void testPatchOk() throws Exception {
        when(eventService.updateByAdmin(updateEventAdminRequest, eventFullDto.getId())).thenReturn(eventFullDto);
        mockMvc.perform(patch("/admin/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventAdminRequest))
                        .contentType(MediaType.APPLICATION_JSON))
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
    @DisplayName("Patch event - title длиннее 120")
    void testPatchTitleMoreThan120() throws Exception {
        updateEventAdminRequest.setTitle("q".repeat(125));
        mockMvc.perform(patch("/admin/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventAdminRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - title меньше 3")
    void testPatchTitleLessThan3() throws Exception {
        updateEventAdminRequest.setTitle("qй");
        mockMvc.perform(patch("/admin/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventAdminRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - title null")
    void testPatchTitleNull() throws Exception {
        updateEventAdminRequest.setTitle(null);
        mockMvc.perform(patch("/admin/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventAdminRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Patch event - annotation длиннее 2000")
    void testPatchAnnotationMoreThan2000() throws Exception {
        updateEventAdminRequest.setAnnotation("q".repeat(2001));
        mockMvc.perform(patch("/admin/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventAdminRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - annotation меньше 20")
    void testPatchAnnotationLessThan20() throws Exception {
        updateEventAdminRequest.setAnnotation("q".repeat(19));
        mockMvc.perform(patch("/admin/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventAdminRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - annotation null")
    void testPatchAnnotationNull() throws Exception {
        updateEventAdminRequest.setAnnotation(null);
        mockMvc.perform(patch("/admin/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventAdminRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Patch event - description длиннее 7000")
    void testPatchDescriptionMoreThan7000() throws Exception {
        updateEventAdminRequest.setDescription("q".repeat(7001));
        mockMvc.perform(patch("/admin/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventAdminRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - description Меньше 20")
    void testPatchDescriptionLessThan20() throws Exception {
        updateEventAdminRequest.setDescription("q".repeat(19));
        mockMvc.perform(patch("/admin/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventAdminRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - description null")
    void testPatchDescriptionNull() throws Exception {
        updateEventAdminRequest.setDescription(null);
        mockMvc.perform(patch("/admin/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventAdminRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get all events by admin  - все параметры")
    void testFindEventsByAdminAllOk() throws Exception {
        list.add(eventFullDto);
        List<Long> categories = new ArrayList<>();
        categories.add(1L);
        List<Long> users = new ArrayList<>();
        users.add(1L);
        List<EventState> states = new ArrayList<>();
        states.add(EventState.PENDING);
        ParamEventsFindByAdmin params = new ParamEventsFindByAdmin()
                .setUsers(users)
                .setStates(states)
                .setCategories(categories)
                .setRangeStart(date)
                .setRangeEnd(date)
                .setFrom(1)
                .setSize(2);
        when(eventService.findEventsByAdmin(params)).thenReturn(list);
        mockMvc.perform(get("/admin/events")
                        .param("users", "1")
                        .param("states", "PENDING")
                        .param("categories", "1")
                        .param("rangeStart", String.valueOf(date))
                        .param("rangeEnd", String.valueOf(date))
                        .param("from", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(eventFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].title", is(eventFullDto.getTitle()), String.class))
                .andExpect(jsonPath("$.[0].annotation", is(eventFullDto.getAnnotation()), String.class))
                .andExpect(jsonPath("$.[0].description", is(eventFullDto.getDescription()), String.class))
                .andExpect(jsonPath("$.[0].eventDate", is(eventFullDto.getEventDate().format(dtf))))
                .andExpect(jsonPath("$.[0].category.id", is(eventFullDto.getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.[0].category.name", is(eventFullDto.getCategory().getName()), String.class))
                .andExpect(jsonPath("$.[0].confirmedRequests", is(eventFullDto.getConfirmedRequests()), Long.class))
                .andExpect(jsonPath("$.[0].createdOn", is(eventFullDto.getCreatedOn().format(dtf))))
                .andExpect(jsonPath("$.[0].initiator.id", is(eventFullDto.getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.name", is(eventFullDto.getInitiator().getName()), String.class))
                .andExpect(jsonPath("$.[0].location.lat", is(eventFullDto.getLocation().getLat()), Double.class))
                .andExpect(jsonPath("$.[0].location.lon", is(eventFullDto.getLocation().getLon()), Double.class))
                .andExpect(jsonPath("$.[0].paid", is(eventFullDto.getPaid()), Boolean.class))
                .andExpect(jsonPath("$.[0].participantLimit", is(eventFullDto.getParticipantLimit()), Integer.class))
                .andExpect(jsonPath("$.[0].publishedOn", is(eventFullDto.getPublishedOn().format(dtf))))
                .andExpect(jsonPath("$.[0].requestModeration", is(eventFullDto.getRequestModeration()), Boolean.class))
                .andExpect(jsonPath("$.[0].state", is(eventFullDto.getState().toString()), String.class))
                .andExpect(jsonPath("$.[0].views", is(eventFullDto.getViews()), Long.class));
    }

    @Test
    @DisplayName("Get all events by admin  - без параметров")
    void testFindEventsByAdminNoParamAllOk() throws Exception {
        list.add(eventFullDto);
        //это дефолтные значения - сравнивать будут с ними в моке
        ParamEventsFindByAdmin params = new ParamEventsFindByAdmin()
                .setFrom(0)
                .setSize(10);
        when(eventService.findEventsByAdmin(params)).thenReturn(list);
        mockMvc.perform(get("/admin/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(eventFullDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].title", is(eventFullDto.getTitle()), String.class))
                .andExpect(jsonPath("$.[0].annotation", is(eventFullDto.getAnnotation()), String.class))
                .andExpect(jsonPath("$.[0].description", is(eventFullDto.getDescription()), String.class))
                .andExpect(jsonPath("$.[0].eventDate", is(eventFullDto.getEventDate().format(dtf))))
                .andExpect(jsonPath("$.[0].category.id", is(eventFullDto.getCategory().getId()), Long.class))
                .andExpect(jsonPath("$.[0].category.name", is(eventFullDto.getCategory().getName()), String.class))
                .andExpect(jsonPath("$.[0].confirmedRequests", is(eventFullDto.getConfirmedRequests()), Long.class))
                .andExpect(jsonPath("$.[0].createdOn", is(eventFullDto.getCreatedOn().format(dtf))))
                .andExpect(jsonPath("$.[0].initiator.id", is(eventFullDto.getInitiator().getId()), Long.class))
                .andExpect(jsonPath("$.[0].initiator.name", is(eventFullDto.getInitiator().getName()), String.class))
                .andExpect(jsonPath("$.[0].location.lat", is(eventFullDto.getLocation().getLat()), Double.class))
                .andExpect(jsonPath("$.[0].location.lon", is(eventFullDto.getLocation().getLon()), Double.class))
                .andExpect(jsonPath("$.[0].paid", is(eventFullDto.getPaid()), Boolean.class))
                .andExpect(jsonPath("$.[0].participantLimit", is(eventFullDto.getParticipantLimit()), Integer.class))
                .andExpect(jsonPath("$.[0].publishedOn", is(eventFullDto.getPublishedOn().format(dtf))))
                .andExpect(jsonPath("$.[0].requestModeration", is(eventFullDto.getRequestModeration()), Boolean.class))
                .andExpect(jsonPath("$.[0].state", is(eventFullDto.getState().toString()), String.class))
                .andExpect(jsonPath("$.[0].views", is(eventFullDto.getViews()), Long.class));
    }

    @Test
    @DisplayName("Get all events by admin  - неправильное состояние")
    void testFindEventsByAdminWrongState() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .param("states", "TEST")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }
}