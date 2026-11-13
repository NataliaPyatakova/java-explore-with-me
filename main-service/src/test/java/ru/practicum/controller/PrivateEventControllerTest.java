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
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enumeration.EventState;
import ru.practicum.enumeration.RequestStatus;
import ru.practicum.service.EventService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PrivateEventController.class)
public class PrivateEventControllerTest {

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
    private final List<EventShortDto> eventShortDtoList = new ArrayList<>();
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
    private final List<ParticipationRequestDto> listParticipationRequestDtoList = new ArrayList<>();
    private final ParticipationRequestDto participationRequestDto = new ParticipationRequestDto()
            .setId(1L)
            .setRequester(1L)
            .setEvent(1L)
            .setStatus(RequestStatus.PENDING)
            .setCreated(date);
    private final NewEventDto newDto = new NewEventDto()
            .setTitle("test_title")
            .setAnnotation("test_annotationnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn")
            .setDescription("test_descriptionnnnnnnnnnnnnnnnnnnnnnnnnnn")
            .setCategory(1L)
            .setEventDate(date)
            .setLocation(locationDto)
            .setPaid(true)
            .setRequestModeration(true)
            .setParticipantLimit(0);
    private final List<Long> requestsId = new ArrayList<>();
    private final EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
            .setRequestIds(requestsId)
            .setStatus(RequestStatus.CONFIRMED);
    private final EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult()
            .setConfirmedRequests(listParticipationRequestDtoList)
            .setRejectedRequests(listParticipationRequestDtoList);
    private final UpdateEventUserRequest updateEventUserRequest = (UpdateEventUserRequest) new UpdateEventUserRequest()
            .setTitle("updateEventUserRequest")
            .setDescription("updateEventUserRequest_description")
            .setAnnotation("updateEventUserRequest_annotation")
            .setEventDate(date.plusDays(1))
            .setCategory(2L)
            .setParticipantLimit(1)
            .setPaid(false)
            .setRequestModeration(false)
            .setLocation(locationDto);

    @Test
    @DisplayName("Get events по пользователю - все хорошо")
    void testFindEventsByUserIdAllOk() throws Exception {
        eventShortDtoList.add(eventShortDto);
        ParamEventFindByUser params = new ParamEventFindByUser(initiatorShort.getId(), 0, 10);
        when(eventService.findEventsByUserId(params)).thenReturn(eventShortDtoList);
        mockMvc.perform(get("/users/" + initiatorShort.getId() + "/events")
                        .param("from", "0")
                        .param("size", "10"))
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
    @DisplayName("Get events по пользователю - без необязательных параметров")
    void testFindEventsByUserIdNoParams() throws Exception {
        eventShortDtoList.add(eventShortDto);
        //это дефолтные значения - сравнивать будут с ними в моке
        ParamEventFindByUser params = new ParamEventFindByUser(initiatorShort.getId(), 0, 10);
        when(eventService.findEventsByUserId(params)).thenReturn(eventShortDtoList);
        mockMvc.perform(get("/users/" + initiatorShort.getId() + "/events"))
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
    @DisplayName("Get event по пользователю и событию - Все хорошо")
    void testFindEventByIdAndByUserId() throws Exception {
        when(eventService.findEventByIdAndByUserId(eventFullDto.getId(), initiatorShort.getId())).thenReturn(eventFullDto);
        mockMvc.perform(get("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId()))
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
    @DisplayName("Get requests по пользователю и событию - Все хорошо")
    void testFindRequestsByEventId() throws Exception {
        listParticipationRequestDtoList.add(participationRequestDto);
        when(eventService.findRequestsByEventId(eventFullDto.getId(), initiatorShort.getId())).thenReturn(listParticipationRequestDtoList);
        mockMvc.perform(get("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId() + "/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(participationRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].event", is(participationRequestDto.getEvent()), Long.class))
                .andExpect(jsonPath("$.[0].requester", is(participationRequestDto.getRequester()), Long.class))
                .andExpect(jsonPath("$.[0].created", is(participationRequestDto.getCreated().format(dtf))))
                .andExpect(jsonPath("$.[0].status", is(participationRequestDto.getStatus().toString())));
    }

    @Test
    @DisplayName("Post event - все хорошо")
    void testSaveOk() throws Exception {
        when(eventService.save(newDto, initiatorShort.getId())).thenReturn(eventFullDto);
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
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
    @DisplayName("Post event - title null")
    void testSaveTitleNull() throws Exception {
        newDto.setTitle(null);
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - title empty")
    void testSaveTitleEmpty() throws Exception {
        newDto.setTitle("");
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - title blank")
    void testSaveTitleBlank() throws Exception {
        newDto.setTitle(" ");
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - title меньше 3")
    void testSaveTitleLessThan3() throws Exception {
        newDto.setTitle("a".repeat(2));
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - title больше 120")
    void testSaveTitleMoreThan3() throws Exception {
        newDto.setTitle("a".repeat(121));
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - annotation null")
    void testSaveAnnotationNull() throws Exception {
        newDto.setAnnotation(null);
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - annotation empty")
    void testSaveAnnotationEmpty() throws Exception {
        newDto.setAnnotation("");
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - annotation blank")
    void testSaveAnnotationBlank() throws Exception {
        newDto.setAnnotation(" ");
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - annotation меньше 20")
    void testSaveAnnotationLessThan20() throws Exception {
        newDto.setAnnotation("a".repeat(19));
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - annotation больше 2000")
    void testSaveAnnotationMoreThan2000() throws Exception {
        newDto.setAnnotation("a".repeat(2001));
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - description null")
    void testSaveDescriptionNull() throws Exception {
        newDto.setDescription(null);
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - description empty")
    void testSaveDescriptionEmpty() throws Exception {
        newDto.setDescription("");
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - description blank")
    void testSaveDescriptionBlank() throws Exception {
        newDto.setDescription(" ");
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - description меньше 20")
    void testSaveDescriptionLessThan20() throws Exception {
        newDto.setDescription("a".repeat(19));
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - description больше 7000")
    void testSaveDescriptionMoreThan7000() throws Exception {
        newDto.setDescription("a".repeat(7001));
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - category null")
    void testSaveCategoryNull() throws Exception {
        newDto.setCategory(null);
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - eventDate null")
    void testSaveEventDateNull() throws Exception {
        newDto.setEventDate(null);
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - location null")
    void testSaveLocationNull() throws Exception {
        newDto.setLocation(null);
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post event - participantLimit null")
    void testSavParticipantLimitNull() throws Exception {
        newDto.setParticipantLimit(null);
        mockMvc.perform(post("/users/" + initiatorShort.getId() + "/events")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Patch requests - все хорошо")
    void testUpdateRequestsStatusOk() throws Exception {
        requestsId.add(1L);
        listParticipationRequestDtoList.add(participationRequestDto);
        when(eventService.updateRequestsStatus(dto, eventFullDto.getId(), initiatorShort.getId())).thenReturn(result);
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId() + "/requests")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests.[0].id", is(participationRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.confirmedRequests.[0].event", is(participationRequestDto.getEvent()), Long.class))
                .andExpect(jsonPath("$.confirmedRequests.[0].requester", is(participationRequestDto.getRequester()), Long.class))
                .andExpect(jsonPath("$.confirmedRequests.[0].created", is(participationRequestDto.getCreated().format(dtf))))
                .andExpect(jsonPath("$.confirmedRequests.[0].status", is(participationRequestDto.getStatus().toString())))
                .andExpect(jsonPath("$.rejectedRequests.[0].id", is(participationRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.rejectedRequests.[0].event", is(participationRequestDto.getEvent()), Long.class))
                .andExpect(jsonPath("$.rejectedRequests.[0].requester", is(participationRequestDto.getRequester()), Long.class))
                .andExpect(jsonPath("$.rejectedRequests.[0].created", is(participationRequestDto.getCreated().format(dtf))))
                .andExpect(jsonPath("$.rejectedRequests.[0].status", is(participationRequestDto.getStatus().toString())));
    }

    @Test
    @DisplayName("Patch requests - status null")
    void testUpdateRequestsStatusNull() throws Exception {
        dto.setStatus(null);
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId() + "/requests")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch requests - requestIds null")
    void testUpdateRequestsStatusIdsNull() throws Exception {
        dto.setRequestIds(null);
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId() + "/requests")
                        .content(mapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - все хорошо")
    void testUpdateByInitiator() throws Exception {
        when(eventService.updateByInitiator(updateEventUserRequest, eventFullDto.getId(), initiatorShort.getId())).thenReturn(eventFullDto);
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventUserRequest))
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
        updateEventUserRequest.setTitle("q".repeat(125));
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - title меньше 3")
    void testPatchTitleLessThan3() throws Exception {
        updateEventUserRequest.setTitle("qй");
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - title null")
    void testPatchTitleNull() throws Exception {
        updateEventUserRequest.setTitle(null);
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Patch event - annotation длиннее 2000")
    void testPatchAnnotationMoreThan2000() throws Exception {
        updateEventUserRequest.setAnnotation("q".repeat(2001));
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - annotation меньше 20")
    void testPatchAnnotationLessThan20() throws Exception {
        updateEventUserRequest.setAnnotation("q".repeat(19));
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - annotation null")
    void testPatchAnnotationNull() throws Exception {
        updateEventUserRequest.setAnnotation(null);
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Patch event - description длиннее 7000")
    void testPatchDescriptionMoreThan7000() throws Exception {
        updateEventUserRequest.setDescription("q".repeat(7001));
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - description Меньше 20")
    void testPatchDescriptionLessThan20() throws Exception {
        updateEventUserRequest.setDescription("q".repeat(19));
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch event - description null")
    void testPatchDescriptionNull() throws Exception {
        updateEventUserRequest.setDescription(null);
        mockMvc.perform(patch("/users/" + initiatorShort.getId() + "/events/" + eventFullDto.getId())
                        .content(mapper.writeValueAsString(updateEventUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}