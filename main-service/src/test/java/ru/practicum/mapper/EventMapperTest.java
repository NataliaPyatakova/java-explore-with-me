package ru.practicum.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.enumeration.AdminEventStateAction;
import ru.practicum.enumeration.EventState;
import ru.practicum.enumeration.UserEventStateAction;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;

import java.time.LocalDateTime;

@SpringBootTest
public class EventMapperTest {

    private final User initiator = new User().setId(1L).setEmail("test_email").setName("test_name");
    private final Category category = new Category().setId(1L).setName("test_category");
    private final Category category2 = new Category().setId(2L).setName("test_category2");
    private final LocalDateTime date = LocalDateTime.now();
    private final Location location = new Location().setLon(123.456).setLat(345.678);
    private final LocationDto locationDto = new LocationDto().setLon(123.456).setLat(345.678);
    private final LocationDto locationDto2 = new LocationDto().setLon(456.789).setLat(45.78);
    private final NewEventDto newDto = new NewEventDto()
            .setTitle("test_title")
            .setDescription("test_description")
            .setAnnotation("test_annotation")
            .setEventDate(date)
            .setPaid(true)
            .setRequestModeration(true)
            .setParticipantLimit(0)
            .setLocationDto(locationDto);
    private final ParamEventSave paramEventSave = new ParamEventSave(initiator, category);
    private final Event event = new Event()
            .setId(2L)
            .setTitle("event")
            .setAnnotation("event_annotation")
            .setDescription("event_description")
            .setEventDate(date)
            .setPaid(true)
            .setRequestModeration(true)
            .setParticipantLimit(0)
            .setLocation(location)
            .setInitiator(initiator)
            .setCategory(category);
    private final ParamEventDto params = new ParamEventDto(0L, 0L);
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
            .setLocationDto(locationDto)
            .setInitiator(initiatorShort)
            .setCategory(categoryDto)
            .setCreatedOn(date)
            .setPublishedOn(date)
            .setConfirmedRequests(0L)
            .setViews(0L)
            .setState(EventState.PENDING);
    private final EventShortDto eventShortDto = new EventShortDto()
            .setId(4L)
            .setTitle("event")
            .setAnnotation("event_annotation")
            .setEventDate(date)
            .setPaid(true)
            .setInitiator(initiatorShort)
            .setCategory(categoryDto)
            .setConfirmedRequests(0L)
            .setViews(0L);
    private final Event foundedEvent = new Event()
            .setId(5L)
            .setTitle("event")
            .setAnnotation("event_annotation")
            .setDescription("event_description")
            .setEventDate(date)
            .setPaid(true)
            .setRequestModeration(true)
            .setParticipantLimit(0)
            .setLocation(location)
            .setInitiator(initiator)
            .setCategory(category)
            .setState(EventState.PENDING);
    private final UpdateEventUserRequest updateEventUserRequest = (UpdateEventUserRequest) new UpdateEventUserRequest()
            .setTitle("updateEventRequest")
            .setDescription("updateEventRequest_description")
            .setAnnotation("updateEventRequest_annotation")
            .setEventDate(date.plusDays(1))
            .setCategory(2L)
            .setParticipantLimit(1)
            .setPaid(false)
            .setRequestModeration(false)
            .setLocationDto(locationDto2);
    private final UpdateEventAdminRequest updateEventAdminRequest = (UpdateEventAdminRequest) new UpdateEventAdminRequest()
            .setTitle("updateEventRequest")
            .setDescription("updateEventRequest_description")
            .setAnnotation("updateEventRequest_annotation")
            .setEventDate(date.plusDays(1))
            .setCategory(2L)
            .setParticipantLimit(1)
            .setPaid(false)
            .setRequestModeration(false)
            .setLocationDto(locationDto2);

    private final ParamEventUpdate paramEventUpdate = new ParamEventUpdate().setCategory(category2);

    @Test
    @DisplayName("Тест маппера из NewEventDto в Event")
    void testMapFromNewEventDtoToEvent() {
        Event result = EventMapper.mapFromNewEventDtoToEvent(newDto, paramEventSave);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newDto.getTitle(), result.getTitle());
        Assertions.assertEquals(newDto.getDescription(), result.getDescription());
        Assertions.assertEquals(newDto.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(newDto.getEventDate(), result.getEventDate());
        Assertions.assertEquals(newDto.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(newDto.getPaid(), result.isPaid());
        Assertions.assertEquals(newDto.getRequestModeration(), result.isRequestModeration());
        Assertions.assertEquals(initiator, result.getInitiator());
        Assertions.assertEquals(category, result.getCategory());
        Assertions.assertEquals(newDto.getLocationDto().getLon(), result.getLocation().getLon());
        Assertions.assertEquals(newDto.getLocationDto().getLat(), result.getLocation().getLat());
    }

    @Test
    @DisplayName("Тест маппера из Event в EventShortDto")
    void testMapFromEventToEventShortDto() {
        EventShortDto result = EventMapper.mapFromEventToEventShortDto(event, params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(event.getTitle(), result.getTitle());
        Assertions.assertEquals(event.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(event.getEventDate(), result.getEventDate());
        Assertions.assertEquals(event.isPaid(), result.getPaid());
        Assertions.assertEquals(params.getConfirmedRequests(), result.getConfirmedRequests());
        Assertions.assertEquals(params.getViews(), result.getViews());
        Assertions.assertEquals(initiator.getId(), result.getInitiator().getId());
        Assertions.assertEquals(category.getId(), result.getCategory().getId());
    }

    @Test
    @DisplayName("Тест маппера из Event в EventFullDto")
    void testMapFromEventToEventFullDto() {
        EventFullDto result = EventMapper.mapFromEventToEventFullDto(event, params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(event.getTitle(), result.getTitle());
        Assertions.assertEquals(event.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(event.getDescription(), result.getDescription());
        Assertions.assertEquals(event.getEventDate(), result.getEventDate());
        Assertions.assertEquals(event.getLocation().getLon(), result.getLocationDto().getLon());
        Assertions.assertEquals(event.getLocation().getLat(), result.getLocationDto().getLat());
        Assertions.assertEquals(event.isPaid(), result.getPaid());
        Assertions.assertEquals(event.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(event.isRequestModeration(), result.getRequestModeration());
        Assertions.assertEquals(event.getPublishedOn(), result.getPublishedOn());
        Assertions.assertEquals(event.getCreatedOn(), result.getCreatedOn());
        Assertions.assertEquals(event.getState(), result.getState());
        Assertions.assertEquals(params.getConfirmedRequests(), result.getConfirmedRequests());
        Assertions.assertEquals(params.getViews(), result.getViews());
        Assertions.assertEquals(initiator.getId(), result.getInitiator().getId());
        Assertions.assertEquals(category.getId(), result.getCategory().getId());
    }

    @Test
    @DisplayName("Тест маппера из EventFullDto в Event")
    void testMapFromEventFullDtoToEvent() {
        Event result = EventMapper.mapFromEventFullDtoToEvent(eventFullDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(eventFullDto.getId(), result.getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getEventDate());
        Assertions.assertEquals(eventFullDto.getLocationDto().getLon(), result.getLocation().getLon());
        Assertions.assertEquals(eventFullDto.getLocationDto().getLat(), result.getLocation().getLat());
        Assertions.assertEquals(eventFullDto.getPaid(), result.isPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.isPaid());
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getPublishedOn());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getCreatedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getState());
        Assertions.assertEquals(eventFullDto.getInitiator().getId(), result.getInitiator().getId());
        Assertions.assertEquals(eventFullDto.getCategory().getId(), result.getCategory().getId());
    }

    @Test
    @DisplayName("Тест маппера из EventShortDto в Event")
    void testMapFromEventShortDtoToEvent() {
        Event result = EventMapper.mapFromEventShortDtoToEvent(eventShortDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(eventShortDto.getId(), result.getId());
        Assertions.assertEquals(eventShortDto.getTitle(), result.getTitle());
        Assertions.assertEquals(eventShortDto.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(eventShortDto.getEventDate(), result.getEventDate());
        Assertions.assertEquals(eventShortDto.getPaid(), result.isPaid());
        Assertions.assertEquals(eventShortDto.getInitiator().getId(), result.getInitiator().getId());
        Assertions.assertEquals(eventShortDto.getCategory().getId(), result.getCategory().getId());
    }

    @Test
    @DisplayName("Тест маппера для updateByInitiator в Event - заполнено все в updateEventUserRequest + отмена")
    void testMapFromUpdateEventUserRequestToEventCancel() {
        updateEventUserRequest.setStateAction(UserEventStateAction.CANCEL_REVIEW);
        Event result = EventMapper.mapFromUpdateEventUserRequestToEvent(foundedEvent, updateEventUserRequest, paramEventUpdate);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedEvent.getId(), result.getId());
        Assertions.assertEquals(updateEventUserRequest.getTitle(), result.getTitle());
        Assertions.assertEquals(updateEventUserRequest.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(updateEventUserRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(updateEventUserRequest.getEventDate(), result.getEventDate());
        Assertions.assertEquals(updateEventUserRequest.getPaid(), result.isPaid());
        Assertions.assertEquals(updateEventUserRequest.getRequestModeration(), result.isRequestModeration());
        Assertions.assertEquals(updateEventUserRequest.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(foundedEvent.getInitiator().getId(), result.getInitiator().getId());
        Assertions.assertEquals(paramEventUpdate.getCategory(), result.getCategory());
        Assertions.assertEquals(updateEventUserRequest.getLocationDto().getLat(), result.getLocation().getLat());
        Assertions.assertEquals(updateEventUserRequest.getLocationDto().getLon(), result.getLocation().getLon());
        Assertions.assertEquals(EventState.CANCELED, result.getState());
    }

    @Test
    @DisplayName("Тест маппера для updateByInitiator в Event - заполнено все в updateEventUserRequest + без отмены")
    void testMapFromUpdateEventUserRequestToEventNoCancel() {
        Event result = EventMapper.mapFromUpdateEventUserRequestToEvent(foundedEvent, updateEventUserRequest, paramEventUpdate);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedEvent.getId(), result.getId());
        Assertions.assertEquals(updateEventUserRequest.getTitle(), result.getTitle());
        Assertions.assertEquals(updateEventUserRequest.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(updateEventUserRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(updateEventUserRequest.getEventDate(), result.getEventDate());
        Assertions.assertEquals(updateEventUserRequest.getPaid(), result.isPaid());
        Assertions.assertEquals(updateEventUserRequest.getRequestModeration(), result.isRequestModeration());
        Assertions.assertEquals(updateEventUserRequest.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(foundedEvent.getInitiator().getId(), result.getInitiator().getId());
        Assertions.assertEquals(paramEventUpdate.getCategory(), result.getCategory());
        Assertions.assertEquals(updateEventUserRequest.getLocationDto().getLat(), result.getLocation().getLat());
        Assertions.assertEquals(updateEventUserRequest.getLocationDto().getLon(), result.getLocation().getLon());
        Assertions.assertEquals(EventState.PENDING, result.getState());
    }

    @Test
    @DisplayName("Тест маппера для updateByInitiator в Event - ничего заполнено все в updateEventUserRequest + отмена")
    void testMapFromUpdateEventUserRequestToEventCancelNoData() {
        updateEventUserRequest.setStateAction(UserEventStateAction.CANCEL_REVIEW)
                .setTitle(null)
                .setDescription(null)
                .setAnnotation(null)
                .setEventDate(null)
                .setCategory(null)
                .setParticipantLimit(null)
                .setPaid(null)
                .setRequestModeration(null)
                .setLocationDto(null);
        Event result = EventMapper.mapFromUpdateEventUserRequestToEvent(foundedEvent, updateEventUserRequest, paramEventUpdate);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedEvent.getId(), result.getId());
        Assertions.assertEquals(foundedEvent.getTitle(), result.getTitle());
        Assertions.assertEquals(foundedEvent.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(foundedEvent.getDescription(), result.getDescription());
        Assertions.assertEquals(foundedEvent.getEventDate(), result.getEventDate());
        Assertions.assertEquals(foundedEvent.isPaid(), result.isPaid());
        Assertions.assertEquals(foundedEvent.isRequestModeration(), result.isRequestModeration());
        Assertions.assertEquals(foundedEvent.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(foundedEvent.getInitiator().getId(), result.getInitiator().getId());
        Assertions.assertEquals(paramEventUpdate.getCategory(), result.getCategory());
        Assertions.assertEquals(foundedEvent.getLocation().getLat(), result.getLocation().getLat());
        Assertions.assertEquals(foundedEvent.getLocation().getLon(), result.getLocation().getLon());
        Assertions.assertEquals(EventState.CANCELED, result.getState());
    }

    @Test
    @DisplayName("Тест маппера для updateByInitiator в Event - заполнено все в updateEventAdminRequest + публикация")
    void testMapFromUpdateEventAdminRequestToEventPublish() {
        updateEventAdminRequest.setStateAction(AdminEventStateAction.PUBLISH_EVENT);
        Event result = EventMapper.mapFromUpdateEventAdminRequestToEvent(foundedEvent, updateEventAdminRequest, paramEventUpdate);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedEvent.getId(), result.getId());
        Assertions.assertEquals(updateEventAdminRequest.getTitle(), result.getTitle());
        Assertions.assertEquals(updateEventAdminRequest.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(updateEventAdminRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(updateEventAdminRequest.getEventDate(), result.getEventDate());
        Assertions.assertEquals(updateEventAdminRequest.getPaid(), result.isPaid());
        Assertions.assertEquals(updateEventAdminRequest.getRequestModeration(), result.isRequestModeration());
        Assertions.assertEquals(updateEventAdminRequest.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(foundedEvent.getInitiator().getId(), result.getInitiator().getId());
        Assertions.assertEquals(paramEventUpdate.getCategory(), result.getCategory());
        Assertions.assertEquals(updateEventAdminRequest.getLocationDto().getLat(), result.getLocation().getLat());
        Assertions.assertEquals(updateEventAdminRequest.getLocationDto().getLon(), result.getLocation().getLon());
        Assertions.assertEquals(EventState.PUBLISHED, result.getState());
        Assertions.assertNotNull(result.getPublishedOn());
    }

    @Test
    @DisplayName("Тест маппера для updateByInitiator в Event - заполнено все в updateEventAdminRequest + отклонение")
    void testMapFromUpdateEventAdminRequestToEventReject() {
        updateEventAdminRequest.setStateAction(AdminEventStateAction.REJECT_EVENT);
        Event result = EventMapper.mapFromUpdateEventAdminRequestToEvent(foundedEvent, updateEventAdminRequest, paramEventUpdate);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedEvent.getId(), result.getId());
        Assertions.assertEquals(updateEventAdminRequest.getTitle(), result.getTitle());
        Assertions.assertEquals(updateEventAdminRequest.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(updateEventAdminRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(updateEventAdminRequest.getEventDate(), result.getEventDate());
        Assertions.assertEquals(updateEventAdminRequest.getPaid(), result.isPaid());
        Assertions.assertEquals(updateEventAdminRequest.getRequestModeration(), result.isRequestModeration());
        Assertions.assertEquals(updateEventAdminRequest.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(foundedEvent.getInitiator().getId(), result.getInitiator().getId());
        Assertions.assertEquals(paramEventUpdate.getCategory(), result.getCategory());
        Assertions.assertEquals(updateEventAdminRequest.getLocationDto().getLat(), result.getLocation().getLat());
        Assertions.assertEquals(updateEventAdminRequest.getLocationDto().getLon(), result.getLocation().getLon());
        Assertions.assertEquals(EventState.CANCELED, result.getState());
    }

    @Test
    @DisplayName("Тест маппера для updateByInitiator в Event - ничего заполнено все в updateEventAdminRequest + отмена")
    void testMapFromUpdateEventAdminRequestToEventNoData() {
        updateEventAdminRequest.setStateAction(AdminEventStateAction.REJECT_EVENT)
                .setTitle(null)
                .setDescription(null)
                .setAnnotation(null)
                .setEventDate(null)
                .setCategory(null)
                .setParticipantLimit(null)
                .setPaid(null)
                .setRequestModeration(null)
                .setLocationDto(null);
        Event result = EventMapper.mapFromUpdateEventAdminRequestToEvent(foundedEvent, updateEventAdminRequest, paramEventUpdate);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedEvent.getId(), result.getId());
        Assertions.assertEquals(foundedEvent.getTitle(), result.getTitle());
        Assertions.assertEquals(foundedEvent.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(foundedEvent.getDescription(), result.getDescription());
        Assertions.assertEquals(foundedEvent.getEventDate(), result.getEventDate());
        Assertions.assertEquals(foundedEvent.isPaid(), result.isPaid());
        Assertions.assertEquals(foundedEvent.isRequestModeration(), result.isRequestModeration());
        Assertions.assertEquals(foundedEvent.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(foundedEvent.getInitiator().getId(), result.getInitiator().getId());
        Assertions.assertEquals(paramEventUpdate.getCategory(), result.getCategory());
        Assertions.assertEquals(foundedEvent.getLocation().getLat(), result.getLocation().getLat());
        Assertions.assertEquals(foundedEvent.getLocation().getLon(), result.getLocation().getLon());
        Assertions.assertEquals(EventState.CANCELED, result.getState());
    }

    @Test
    @DisplayName("Тест маппера для updateByInitiator в Event - заполнено все в updateEventAdminRequest + состояние не меняем")
    void testMapFromUpdateEventAdminRequestToEvent() {
        Event result = EventMapper.mapFromUpdateEventAdminRequestToEvent(foundedEvent, updateEventAdminRequest, paramEventUpdate);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedEvent.getId(), result.getId());
        Assertions.assertEquals(updateEventAdminRequest.getTitle(), result.getTitle());
        Assertions.assertEquals(updateEventAdminRequest.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(updateEventAdminRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(updateEventAdminRequest.getEventDate(), result.getEventDate());
        Assertions.assertEquals(updateEventAdminRequest.getPaid(), result.isPaid());
        Assertions.assertEquals(updateEventAdminRequest.getRequestModeration(), result.isRequestModeration());
        Assertions.assertEquals(updateEventAdminRequest.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(foundedEvent.getInitiator().getId(), result.getInitiator().getId());
        Assertions.assertEquals(paramEventUpdate.getCategory(), result.getCategory());
        Assertions.assertEquals(updateEventAdminRequest.getLocationDto().getLat(), result.getLocation().getLat());
        Assertions.assertEquals(updateEventAdminRequest.getLocationDto().getLon(), result.getLocation().getLon());
        Assertions.assertEquals(foundedEvent.getState(), result.getState());
    }

    @Test
    @DisplayName("Тест маппера для updateByInitiator в Event - заполнено все в updateEventAdminRequest + нет категории")
    void testMapFromUpdateEventAdminRequestToEventNoCategory() {
        paramEventUpdate.setCategory(null);
        Event result = EventMapper.mapFromUpdateEventAdminRequestToEvent(foundedEvent, updateEventAdminRequest, paramEventUpdate);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedEvent.getId(), result.getId());
        Assertions.assertEquals(updateEventAdminRequest.getTitle(), result.getTitle());
        Assertions.assertEquals(updateEventAdminRequest.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(updateEventAdminRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(updateEventAdminRequest.getEventDate(), result.getEventDate());
        Assertions.assertEquals(updateEventAdminRequest.getPaid(), result.isPaid());
        Assertions.assertEquals(updateEventAdminRequest.getRequestModeration(), result.isRequestModeration());
        Assertions.assertEquals(updateEventAdminRequest.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(foundedEvent.getInitiator().getId(), result.getInitiator().getId());
        Assertions.assertEquals(foundedEvent.getCategory(), result.getCategory());
        Assertions.assertEquals(updateEventAdminRequest.getLocationDto().getLat(), result.getLocation().getLat());
        Assertions.assertEquals(updateEventAdminRequest.getLocationDto().getLon(), result.getLocation().getLon());
        Assertions.assertEquals(foundedEvent.getState(), result.getState());
    }
}
