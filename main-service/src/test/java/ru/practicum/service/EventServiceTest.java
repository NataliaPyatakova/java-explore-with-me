package ru.practicum.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enumeration.AdminEventStateAction;
import ru.practicum.enumeration.EventState;
import ru.practicum.enumeration.RequestStatus;
import ru.practicum.enumeration.UserEventStateAction;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Location;
import ru.practicum.model.Request;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class EventServiceTest {

    @Autowired
    private EventService eventService;
    private final EntityManager em;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LocalDateTime date = LocalDateTime.now().plusDays(1);
    private final LocalDateTime now = LocalDateTime.now();
    private final Category category = new Category().setName("test_category");
    private final Category category2 = new Category().setName("test_category2");
    private final Location location = new Location().setLat(15.15).setLon(16.16);
    private final LocationDto locationDto = new LocationDto().setLat(15.15).setLon(16.16);
    private final LocationDto locationDto2 = new LocationDto().setLat(5.8).setLon(8.7);
    private final User initiator = new User()
            .setEmail("email_initiator@")
            .setName("initiator_name");
    private final User initiator2 = new User()
            .setEmail("email_initiator2@")
            .setName("initiator_name2");
    private final NewEventDto newEventDto = new NewEventDto()
            .setTitle("title")
            .setDescription("description")
            .setAnnotation("annotation")
            .setEventDate(date)
            .setLocation(locationDto)
            .setPaid(true)
            .setParticipantLimit(0)
            .setRequestModeration(true);
    private final NewEventDto newEventDto2 = new NewEventDto()
            .setTitle("title2")
            .setDescription("description2")
            .setAnnotation("annotation2")
            .setEventDate(date)
            .setLocation(locationDto)
            .setPaid(true)
            .setParticipantLimit(0)
            .setRequestModeration(true);
    private final User requester = new User()
            .setEmail("requester_email")
            .setName("requester_name");
    private final UpdateEventUserRequest updateEventUserRequest = (UpdateEventUserRequest) new UpdateEventUserRequest()
            .setTitle("updateEventUserRequest")
            .setDescription("updateEventUserRequest_description")
            .setAnnotation("updateEventUserRequest_annotation")
            .setEventDate(date.plusDays(1))
            .setParticipantLimit(1)
            .setPaid(false)
            .setRequestModeration(false)
            .setLocation(locationDto2);
    private final UpdateEventAdminRequest updateEventAdminRequest = (UpdateEventAdminRequest) new UpdateEventAdminRequest()
            .setTitle("UpdateEventAdminRequest")
            .setDescription("UpdateEventAdminRequest_description")
            .setAnnotation("UpdateEventAdminRequest_annotation")
            .setEventDate(date.plusDays(1))
            .setParticipantLimit(1)
            .setPaid(false)
            .setRequestModeration(false)
            .setLocation(locationDto2);

    @Test
    @DisplayName("Сохранение event - все хорошо")
    public void testSave() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto result = eventService.save(newEventDto, initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newEventDto.getTitle(), result.getTitle());
        Assertions.assertEquals(newEventDto.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(newEventDto.getDescription(), result.getDescription());
        Assertions.assertEquals(newEventDto.getEventDate(), result.getEventDate());
        Assertions.assertEquals(newEventDto.getPaid(), result.getPaid());
        Assertions.assertEquals(newEventDto.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(newEventDto.getRequestModeration(), result.getRequestModeration());
        Assertions.assertEquals(initiator.getId(), result.getInitiator().getId());
        Assertions.assertEquals(category.getId(), result.getCategory().getId());
        Assertions.assertEquals(location.getLat(), result.getLocation().getLat());
        Assertions.assertEquals(location.getLon(), result.getLocation().getLon());
        Assertions.assertEquals(0, result.getConfirmedRequests());
        Assertions.assertNotNull(result.getCreatedOn());
        Assertions.assertNull(result.getPublishedOn());
        Assertions.assertEquals(EventState.PENDING, result.getState());
        Assertions.assertEquals(0, result.getViews());
    }

    @Test
    @DisplayName("Сохранение event - неверная дата")
    public void testSaveWrongDate() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        newEventDto.setEventDate(now.plusHours(1)); //событие через час
        try {
            eventService.save(newEventDto, initiator.getId());
        } catch (ValidationException ex) {
            Assertions.assertEquals("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: "
                    + newEventDto.getEventDate().format(dtf), ex.getReason());
            Assertions.assertEquals("For the requested operation the conditions are not met.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Сохранение event - неизвестный пользователь")
    public void testSaveWrongUser() {
        try {
            eventService.save(newEventDto, 999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Сохранение event - неизвестный категория")
    public void testSaveWrongCategory() {
        em.persist(initiator);
        newEventDto.setCategory(999L);
        try {
            eventService.save(newEventDto, initiator.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("Category with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление event от user - неизвестное событие")
    public void testUpdateByInitiatorWrongEventId() {
        em.persist(initiator);
        try {
            eventService.updateByInitiator(updateEventUserRequest, 999L, initiator.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("Event with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление event от user - неизвестный user")
    public void testUpdateByInitiatorWrongUserId() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        try {
            eventService.updateByInitiator(updateEventUserRequest, eventFullDto.getId(), 999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление event от user - неверное состояние")
    public void testUpdateByInitiatorWrongState() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //обновляем статус от админа - публикуем
        updateEventAdminRequest.setTitle(null);
        updateEventAdminRequest.setAnnotation(null);
        updateEventAdminRequest.setDescription(null);
        updateEventAdminRequest.setLocation(null);
        updateEventAdminRequest.setRequestModeration(null);
        updateEventAdminRequest.setParticipantLimit(null);
        updateEventAdminRequest.setCategory(null);
        updateEventAdminRequest.setEventDate(null);
        updateEventAdminRequest.setPaid(null);
        updateEventAdminRequest.setStateAction(AdminEventStateAction.PUBLISH_EVENT);
        eventService.updateByAdmin(updateEventAdminRequest, eventFullDto.getId());
        try {
            //пытаемся изменить опубликованное
            eventService.updateByInitiator(updateEventUserRequest, eventFullDto.getId(), initiator.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Field: state. Error: изменить можно только отмененные события или события в " +
                    "состоянии ожидания модерации. Value: PUBLISHED", e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление event от user - неверная дата")
    public void testUpdateByInitiatorWrongDate() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        updateEventUserRequest.setEventDate(now.plusHours(1));
        try {
            //пытаемся изменить опубликованное
            eventService.updateByInitiator(updateEventUserRequest, eventFullDto.getId(), initiator.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Field: eventDate. Error: должно содержать дату, " +
                    "которая еще не наступила. Value: " + now.plusHours(1).format(dtf), e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление event от user - пользователь должен быть инициатором события")
    public void testUpdateByInitiatorWrongInitiator() {
        //сейв
        em.persist(initiator);
        em.persist(initiator2);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        try {
            eventService.updateByInitiator(updateEventUserRequest, eventFullDto.getId(), initiator2.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Current user isn't an initiator of current event", e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление event от user - заполнены все поля")
    public void testUpdateByInitiatorWithCategory() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //апдейт
        em.persist(category2);
        updateEventUserRequest.setCategory(category2.getId());
        updateEventUserRequest.setStateAction(UserEventStateAction.SEND_TO_REVIEW);
        EventFullDto result = eventService.updateByInitiator(updateEventUserRequest, eventFullDto.getId(), initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(eventFullDto.getId(), result.getId());
        Assertions.assertEquals(updateEventUserRequest.getTitle(), result.getTitle());
        Assertions.assertEquals(updateEventUserRequest.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(updateEventUserRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(updateEventUserRequest.getEventDate(), result.getEventDate());
        Assertions.assertEquals(updateEventUserRequest.getPaid(), result.getPaid());
        Assertions.assertEquals(updateEventUserRequest.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(updateEventUserRequest.getRequestModeration(), result.getRequestModeration());
        Assertions.assertEquals(updateEventUserRequest.getCategory(), result.getCategory().getId());
        Assertions.assertEquals(updateEventUserRequest.getLocation(), result.getLocation());
        //апдейт зависит от входных параметров
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getPublishedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getState());
        //никогда не должны апдейтиться
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getInitiator());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getCreatedOn());
        //расчетные
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getViews(), result.getViews());
    }

    @Test
    @DisplayName("Обновление event от user - заполнены все поля кроме категории")
    public void testUpdateByInitiatorEmptyCategory() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //апдейт
        updateEventUserRequest.setStateAction(UserEventStateAction.SEND_TO_REVIEW);
        EventFullDto result = eventService.updateByInitiator(updateEventUserRequest, eventFullDto.getId(), initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(eventFullDto.getId(), result.getId());
        Assertions.assertEquals(updateEventUserRequest.getTitle(), result.getTitle());
        Assertions.assertEquals(updateEventUserRequest.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(updateEventUserRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(updateEventUserRequest.getEventDate(), result.getEventDate());
        Assertions.assertEquals(updateEventUserRequest.getPaid(), result.getPaid());
        Assertions.assertEquals(updateEventUserRequest.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(updateEventUserRequest.getRequestModeration(), result.getRequestModeration());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getCategory());
        Assertions.assertEquals(updateEventUserRequest.getLocation(), result.getLocation());
        //апдейт зависит от входных параметров
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getPublishedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getState());
        //никогда не должны апдейтиться
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getInitiator());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getCreatedOn());
        //расчетные
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getViews(), result.getViews());
    }

    @Test
    @DisplayName("Обновление event от user - заполнены все поля кроме location")
    public void testUpdateByInitiatorNoLocation() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //апдейт
        em.persist(category2);
        updateEventUserRequest.setCategory(category2.getId());
        updateEventUserRequest.setStateAction(UserEventStateAction.SEND_TO_REVIEW);
        updateEventUserRequest.setLocation(null);
        EventFullDto result = eventService.updateByInitiator(updateEventUserRequest, eventFullDto.getId(), initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(eventFullDto.getId(), result.getId());
        Assertions.assertEquals(updateEventUserRequest.getTitle(), result.getTitle());
        Assertions.assertEquals(updateEventUserRequest.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(updateEventUserRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(updateEventUserRequest.getEventDate(), result.getEventDate());
        Assertions.assertEquals(updateEventUserRequest.getPaid(), result.getPaid());
        Assertions.assertEquals(updateEventUserRequest.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(updateEventUserRequest.getRequestModeration(), result.getRequestModeration());
        Assertions.assertEquals(updateEventUserRequest.getCategory(), result.getCategory().getId());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getLocation());
        //апдейт зависит от входных параметров
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getPublishedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getState());
        //никогда не должны апдейтиться
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getInitiator());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getCreatedOn());
        //расчетные
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getViews(), result.getViews());
    }

    @Test
    @DisplayName("Обновление event от user - меняем только статус - на ревью")
    public void testUpdateByInitiatorOnlyState() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //апдейт
        updateEventUserRequest.setTitle(null);
        updateEventUserRequest.setAnnotation(null);
        updateEventUserRequest.setDescription(null);
        updateEventUserRequest.setEventDate(null);
        updateEventUserRequest.setPaid(null);
        updateEventUserRequest.setParticipantLimit(null);
        updateEventUserRequest.setRequestModeration(null);
        updateEventUserRequest.setCategory(null);
        updateEventUserRequest.setLocation(null);
        updateEventUserRequest.setStateAction(UserEventStateAction.SEND_TO_REVIEW);
        EventFullDto result = eventService.updateByInitiator(updateEventUserRequest, eventFullDto.getId(), initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(eventFullDto.getId(), result.getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.getRequestModeration());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getCategory());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getLocation());
        //апдейт зависит от входных параметров
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getPublishedOn());
        //при отправке на ревью статус не меняется (?)
        Assertions.assertEquals(eventFullDto.getState(), result.getState());
        //никогда не должны апдейтиться
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getInitiator());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getCreatedOn());
        //расчетные
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getViews(), result.getViews());
    }

    @Test
    @DisplayName("Обновление event от user - меняем только статус - отмена ревью")
    public void testUpdateByInitiatorOnlyStateCancel() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //апдейт
        updateEventUserRequest.setTitle(null);
        updateEventUserRequest.setAnnotation(null);
        updateEventUserRequest.setDescription(null);
        updateEventUserRequest.setEventDate(null);
        updateEventUserRequest.setPaid(null);
        updateEventUserRequest.setParticipantLimit(null);
        updateEventUserRequest.setRequestModeration(null);
        updateEventUserRequest.setCategory(null);
        updateEventUserRequest.setLocation(null);
        updateEventUserRequest.setStateAction(UserEventStateAction.CANCEL_REVIEW);
        EventFullDto result = eventService.updateByInitiator(updateEventUserRequest, eventFullDto.getId(), initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(eventFullDto.getId(), result.getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.getRequestModeration());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getCategory());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getLocation());
        //апдейт зависит от входных параметров
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getPublishedOn());
        Assertions.assertEquals(EventState.CANCELED, result.getState());
        //никогда не должны апдейтиться
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getInitiator());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getCreatedOn());
        //расчетные
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getViews(), result.getViews());
    }

    @Test
    @DisplayName("Обновление event от admin - неизвестное событие")
    public void testUpdateByAdminWrongEventId() {
        em.persist(initiator);
        try {
            eventService.updateByAdmin(updateEventAdminRequest, 999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Event with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление event от admin - неверная дата")
    public void testUpdateByAdminWrongDate() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        updateEventUserRequest.setEventDate(now.plusMinutes(5));
        try {
            eventService.updateByAdmin(updateEventAdminRequest, eventFullDto.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Field: eventDate. Error: должно содержать дату, " +
                    "которая еще не наступила. Value: " + now.plusMinutes(5).format(dtf), e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление event от admin - публикация - неверное состояние")
    public void testUpdateByAdminWrongState() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //обновляем статус от админа - публикуем
        updateEventAdminRequest.setTitle(null);
        updateEventAdminRequest.setAnnotation(null);
        updateEventAdminRequest.setDescription(null);
        updateEventAdminRequest.setLocation(null);
        updateEventAdminRequest.setRequestModeration(null);
        updateEventAdminRequest.setParticipantLimit(null);
        updateEventAdminRequest.setCategory(null);
        updateEventAdminRequest.setEventDate(null);
        updateEventAdminRequest.setPaid(null);
        updateEventAdminRequest.setStateAction(AdminEventStateAction.PUBLISH_EVENT);
        eventService.updateByAdmin(updateEventAdminRequest, eventFullDto.getId());
        try {
            //пытаемся изменить опубликованное повторно
            eventService.updateByAdmin(updateEventAdminRequest, eventFullDto.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Cannot publish the event because it's not in the right state: PUBLISHED", e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление event от admin - отклонение публикации - неверное состояние")
    public void testUpdateByAdminWrongState2() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //обновляем статус от админа - публикуем
        updateEventAdminRequest.setTitle(null);
        updateEventAdminRequest.setAnnotation(null);
        updateEventAdminRequest.setDescription(null);
        updateEventAdminRequest.setLocation(null);
        updateEventAdminRequest.setRequestModeration(null);
        updateEventAdminRequest.setParticipantLimit(null);
        updateEventAdminRequest.setCategory(null);
        updateEventAdminRequest.setEventDate(null);
        updateEventAdminRequest.setPaid(null);
        updateEventAdminRequest.setStateAction(AdminEventStateAction.PUBLISH_EVENT);
        eventService.updateByAdmin(updateEventAdminRequest, eventFullDto.getId());
        try {
            //пытаемся изменить опубликованное повторно
            updateEventAdminRequest.setStateAction(AdminEventStateAction.REJECT_EVENT);
            eventService.updateByAdmin(updateEventAdminRequest, eventFullDto.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Cannot reject the event because it's not in the right state: PUBLISHED", e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление event от admin - заполнены все поля - публикация")
    public void testUpdateByAdminAllPublish() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //апдейт
        em.persist(category2);
        updateEventAdminRequest.setCategory(category2.getId());
        updateEventAdminRequest.setStateAction(AdminEventStateAction.PUBLISH_EVENT);
        EventFullDto result = eventService.updateByAdmin(updateEventAdminRequest, eventFullDto.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(eventFullDto.getId(), result.getId());
        Assertions.assertEquals(updateEventAdminRequest.getTitle(), result.getTitle());
        Assertions.assertEquals(updateEventAdminRequest.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(updateEventAdminRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(updateEventAdminRequest.getEventDate(), result.getEventDate());
        Assertions.assertEquals(updateEventAdminRequest.getPaid(), result.getPaid());
        Assertions.assertEquals(updateEventAdminRequest.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(updateEventAdminRequest.getRequestModeration(), result.getRequestModeration());
        Assertions.assertEquals(updateEventAdminRequest.getCategory(), result.getCategory().getId());
        Assertions.assertEquals(updateEventAdminRequest.getLocation(), result.getLocation());
        //апдейт зависит от входных параметров
        Assertions.assertNotNull(result.getPublishedOn());
        Assertions.assertEquals(EventState.PUBLISHED, result.getState());
        //никогда не должны апдейтиться
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getInitiator());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getCreatedOn());
        //расчетные
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getViews(), result.getViews());
    }

    @Test
    @DisplayName("Обновление event от admin - заполнены все поля - отклонение публикации")
    public void testUpdateByAdminAllReject() {
        //сейв
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //апдейт
        em.persist(category2);
        updateEventAdminRequest.setCategory(category2.getId());
        updateEventAdminRequest.setStateAction(AdminEventStateAction.REJECT_EVENT);
        EventFullDto result = eventService.updateByAdmin(updateEventAdminRequest, eventFullDto.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(eventFullDto.getId(), result.getId());
        Assertions.assertEquals(updateEventAdminRequest.getTitle(), result.getTitle());
        Assertions.assertEquals(updateEventAdminRequest.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(updateEventAdminRequest.getDescription(), result.getDescription());
        Assertions.assertEquals(updateEventAdminRequest.getEventDate(), result.getEventDate());
        Assertions.assertEquals(updateEventAdminRequest.getPaid(), result.getPaid());
        Assertions.assertEquals(updateEventAdminRequest.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(updateEventAdminRequest.getRequestModeration(), result.getRequestModeration());
        Assertions.assertEquals(updateEventAdminRequest.getCategory(), result.getCategory().getId());
        Assertions.assertEquals(updateEventAdminRequest.getLocation(), result.getLocation());
        //апдейт зависит от входных параметров
        Assertions.assertNull(result.getPublishedOn());
        Assertions.assertEquals(EventState.CANCELED, result.getState());
        //никогда не должны апдейтиться
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getInitiator());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getCreatedOn());
        //расчетные
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getViews(), result.getViews());
    }

    @Test
    @DisplayName("Поиск event по eventId и userId - все хорошо")
    void testFindEventByIdAndByUserId() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        EventFullDto result = eventService.findEventByIdAndByUserId(eventFullDto.getId(), initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(eventFullDto.getId(), result.getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.getRequestModeration());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getCategory());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getLocation());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getCreatedOn());
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getPublishedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getState());
        Assertions.assertEquals(eventFullDto.getViews(), result.getViews());
    }

    @Test
    @DisplayName("Поиск event по eventId и userId - неизвестный пользователь")
    public void testFindEventByIdAndByUserIdWrongUserId() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        try {
            eventService.findEventByIdAndByUserId(eventFullDto.getId(), 999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск event по eventId и userId - неизвестное событие")
    public void testFindEventByIdAndByUserIdWrongEventId() {
        em.persist(initiator);
        try {
            eventService.findEventByIdAndByUserId(999L, initiator.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("Event with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск events по userId - все хорошо")
    void testFindEventsByUserId() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        ParamEventFindByUser params = new ParamEventFindByUser(initiator.getId(), 0, 10);
        List<EventShortDto> result = eventService.findEventsByUserId(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(eventFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getFirst().getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getFirst().getAnnotation());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getFirst().getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getFirst().getPaid());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getFirst().getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getFirst().getCategory());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getFirst().getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getViews(), result.getFirst().getViews());
    }

    @Test
    @DisplayName("Поиск events по userId - неизвестный пользователь")
    public void testFindEventsByUserIdWrongUserId() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        eventService.save(newEventDto, initiator.getId());
        ParamEventFindByUser params = new ParamEventFindByUser(999L, 0, 10);
        try {
            eventService.findEventsByUserId(params);
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск events по userId - фильтрация по userId")
    void testFindEventsByUserIdFilterUserId() {
        em.persist(initiator);
        em.persist(initiator2);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        newEventDto2.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //второе событие с другим инициатором
        eventService.save(newEventDto2, initiator2.getId());
        ParamEventFindByUser params = new ParamEventFindByUser(initiator.getId(), 0, 10);
        List<EventShortDto> result = eventService.findEventsByUserId(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(eventFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getFirst().getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getFirst().getAnnotation());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getFirst().getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getFirst().getPaid());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getFirst().getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getFirst().getCategory());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getFirst().getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getViews(), result.getFirst().getViews());
    }

    @Test
    @DisplayName("Поиск events по userId - фильтрация по size")
    void testFindEventsByUserIdFilterSize() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        newEventDto2.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        eventService.save(newEventDto2, initiator.getId());
        ParamEventFindByUser params = new ParamEventFindByUser(initiator.getId(), 0, 1);
        List<EventShortDto> result = eventService.findEventsByUserId(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(eventFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getFirst().getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getFirst().getAnnotation());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getFirst().getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getFirst().getPaid());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getFirst().getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getFirst().getCategory());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getFirst().getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getViews(), result.getFirst().getViews());
    }

    @Test
    @DisplayName("Поиск events по параметрам от admin - без параметров")
    void testFindEventsByAdmin() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        newEventDto2.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        ParamEventsFindByAdmin params = new ParamEventsFindByAdmin()
                .setFrom(0)
                .setSize(10);
        List<EventFullDto> result = eventService.findEventsByAdmin(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(eventFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getFirst().getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getFirst().getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getFirst().getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getFirst().getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getFirst().getPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getFirst().getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.getFirst().getRequestModeration());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getFirst().getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getFirst().getCategory());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getFirst().getLocation());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getFirst().getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getFirst().getCreatedOn());
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getFirst().getPublishedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getFirst().getState());
        Assertions.assertEquals(eventFullDto.getViews(), result.getFirst().getViews());
    }

    @Test
    @DisplayName("Поиск events по параметрам от admin - по пользователю")
    void testFindEventsByAdminByUserId() {
        em.persist(initiator);
        em.persist(initiator2);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        newEventDto2.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //второе событие у другого пользователя
        eventService.save(newEventDto2, initiator2.getId());
        List<Long> users = new ArrayList<>();
        users.add(initiator.getId());
        ParamEventsFindByAdmin params = new ParamEventsFindByAdmin()
                .setUsers(users)
                .setFrom(0)
                .setSize(10);
        List<EventFullDto> result = eventService.findEventsByAdmin(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(eventFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getFirst().getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getFirst().getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getFirst().getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getFirst().getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getFirst().getPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getFirst().getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.getFirst().getRequestModeration());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getFirst().getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getFirst().getCategory());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getFirst().getLocation());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getFirst().getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getFirst().getCreatedOn());
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getFirst().getPublishedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getFirst().getState());
        Assertions.assertEquals(eventFullDto.getViews(), result.getFirst().getViews());
    }

    @Test
    @DisplayName("Поиск events по параметрам от admin - по состоянию")
    void testFindEventsByAdminByState() {
        em.persist(initiator);
        em.persist(initiator2);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        newEventDto2.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        List<EventState> state = new ArrayList<>();
        state.add(eventFullDto.getState());
        ParamEventsFindByAdmin params = new ParamEventsFindByAdmin()
                .setStates(state)
                .setFrom(0)
                .setSize(10);
        List<EventFullDto> result = eventService.findEventsByAdmin(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(eventFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getFirst().getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getFirst().getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getFirst().getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getFirst().getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getFirst().getPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getFirst().getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.getFirst().getRequestModeration());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getFirst().getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getFirst().getCategory());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getFirst().getLocation());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getFirst().getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getFirst().getCreatedOn());
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getFirst().getPublishedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getFirst().getState());
        Assertions.assertEquals(eventFullDto.getViews(), result.getFirst().getViews());
    }

    @Test
    @DisplayName("Поиск events по параметрам от admin - по категории")
    void testFindEventsByAdminByCategory() {
        em.persist(initiator);
        em.persist(initiator2);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        newEventDto2.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        List<Long> categories = new ArrayList<>();
        categories.add(eventFullDto.getCategory().getId());
        ParamEventsFindByAdmin params = new ParamEventsFindByAdmin()
                .setCategories(categories)
                .setFrom(0)
                .setSize(10);
        List<EventFullDto> result = eventService.findEventsByAdmin(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(eventFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getFirst().getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getFirst().getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getFirst().getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getFirst().getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getFirst().getPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getFirst().getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.getFirst().getRequestModeration());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getFirst().getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getFirst().getCategory());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getFirst().getLocation());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getFirst().getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getFirst().getCreatedOn());
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getFirst().getPublishedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getFirst().getState());
        Assertions.assertEquals(eventFullDto.getViews(), result.getFirst().getViews());
    }

    @Test
    @DisplayName("Поиск events по параметрам от admin - по дате начала")
    void testFindEventsByAdminByRangeStart() {
        em.persist(initiator);
        em.persist(initiator2);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        newEventDto2.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        ParamEventsFindByAdmin params = new ParamEventsFindByAdmin()
                .setRangeStart(newEventDto.getEventDate().minusDays(1))
                .setFrom(0)
                .setSize(10);
        List<EventFullDto> result = eventService.findEventsByAdmin(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(eventFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getFirst().getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getFirst().getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getFirst().getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getFirst().getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getFirst().getPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getFirst().getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.getFirst().getRequestModeration());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getFirst().getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getFirst().getCategory());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getFirst().getLocation());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getFirst().getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getFirst().getCreatedOn());
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getFirst().getPublishedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getFirst().getState());
        Assertions.assertEquals(eventFullDto.getViews(), result.getFirst().getViews());
    }

    @Test
    @DisplayName("Поиск events по параметрам от admin - по дате окончания")
    void testFindEventsByAdminByRangeEnd() {
        em.persist(initiator);
        em.persist(initiator2);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        newEventDto2.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        ParamEventsFindByAdmin params = new ParamEventsFindByAdmin()
                .setRangeEnd(newEventDto.getEventDate().plusDays(1))
                .setFrom(0)
                .setSize(10);
        List<EventFullDto> result = eventService.findEventsByAdmin(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(eventFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getFirst().getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getFirst().getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getFirst().getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getFirst().getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getFirst().getPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getFirst().getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.getFirst().getRequestModeration());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getFirst().getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getFirst().getCategory());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getFirst().getLocation());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getFirst().getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getFirst().getCreatedOn());
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getFirst().getPublishedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getFirst().getState());
        Assertions.assertEquals(eventFullDto.getViews(), result.getFirst().getViews());
    }

    @Test
    @DisplayName("Поиск events по параметрам от admin - по дате начала и окончания")
    void testFindEventsByAdminByRangeStartAndRangeEnd() {
        em.persist(initiator);
        em.persist(initiator2);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        newEventDto2.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        ParamEventsFindByAdmin params = new ParamEventsFindByAdmin()
                .setRangeStart(newEventDto.getEventDate().minusDays(1))
                .setRangeEnd(newEventDto.getEventDate().plusDays(1))
                .setFrom(0)
                .setSize(10);
        List<EventFullDto> result = eventService.findEventsByAdmin(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(eventFullDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getFirst().getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getFirst().getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getFirst().getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getFirst().getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getFirst().getPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getFirst().getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.getFirst().getRequestModeration());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getFirst().getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getFirst().getCategory());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getFirst().getLocation());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getFirst().getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getFirst().getCreatedOn());
        Assertions.assertEquals(eventFullDto.getPublishedOn(), result.getFirst().getPublishedOn());
        Assertions.assertEquals(eventFullDto.getState(), result.getFirst().getState());
        Assertions.assertEquals(eventFullDto.getViews(), result.getFirst().getViews());
    }

    @Test
    @DisplayName("Поиск events по параметрам от admin - по дате начала и окончания - событие вне диапазона")
    void testFindEventsByAdminByRangeStartAndRangeEndNoEvent() {
        em.persist(initiator);
        em.persist(initiator2);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        newEventDto2.setCategory(category.getId());
        eventService.save(newEventDto, initiator.getId());
        ParamEventsFindByAdmin params = new ParamEventsFindByAdmin()
                .setRangeStart(newEventDto.getEventDate().minusDays(1))
                .setRangeEnd(newEventDto.getEventDate().minusHours(5))
                .setFrom(0)
                .setSize(10);
        List<EventFullDto> result = eventService.findEventsByAdmin(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск опубликованного event - все хорошо")
    void testFindPublishedEventByIdAllOk() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        //обновляем статус от админа - публикуем
        updateEventAdminRequest.setTitle(null);
        updateEventAdminRequest.setAnnotation(null);
        updateEventAdminRequest.setDescription(null);
        updateEventAdminRequest.setLocation(null);
        updateEventAdminRequest.setRequestModeration(null);
        updateEventAdminRequest.setParticipantLimit(null);
        updateEventAdminRequest.setCategory(null);
        updateEventAdminRequest.setEventDate(null);
        updateEventAdminRequest.setPaid(null);
        updateEventAdminRequest.setStateAction(AdminEventStateAction.PUBLISH_EVENT);
        eventService.updateByAdmin(updateEventAdminRequest, eventFullDto.getId());
        EventFullDto result = eventService.findPublishedEventById(eventFullDto.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(eventFullDto.getId(), result.getId());
        Assertions.assertEquals(eventFullDto.getTitle(), result.getTitle());
        Assertions.assertEquals(eventFullDto.getAnnotation(), result.getAnnotation());
        Assertions.assertEquals(eventFullDto.getDescription(), result.getDescription());
        Assertions.assertEquals(eventFullDto.getEventDate(), result.getEventDate());
        Assertions.assertEquals(eventFullDto.getPaid(), result.getPaid());
        Assertions.assertEquals(eventFullDto.getParticipantLimit(), result.getParticipantLimit());
        Assertions.assertEquals(eventFullDto.getRequestModeration(), result.getRequestModeration());
        Assertions.assertEquals(eventFullDto.getInitiator(), result.getInitiator());
        Assertions.assertEquals(eventFullDto.getCategory(), result.getCategory());
        Assertions.assertEquals(eventFullDto.getLocation(), result.getLocation());
        Assertions.assertEquals(eventFullDto.getConfirmedRequests(), result.getConfirmedRequests());
        Assertions.assertEquals(eventFullDto.getCreatedOn(), result.getCreatedOn());
        Assertions.assertNotNull(result.getPublishedOn());
        Assertions.assertEquals(EventState.PUBLISHED, result.getState());
        Assertions.assertEquals(eventFullDto.getViews(), result.getViews());
    }

    @Test
    @DisplayName("Поиск опубликованного event - не публиковали")
    void testFindPublishedEventByIdNotPublished() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        try {
            eventService.findPublishedEventById(eventFullDto.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("Event with id=" + eventFullDto.getId() + " was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск опубликованного event - нет такого события")
    void testFindPublishedEventByIdWrongId() {
        try {
            eventService.findPublishedEventById(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Event with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск requests по eventId и userId - нет запросов")
    void testFindRequestsByEventIdNoRequests() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        List<ParticipationRequestDto> result = eventService.findRequestsByEventId(eventFullDto.getId(), initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск requests по eventId и userId - есть запросы")
    void testFindRequestsByEventIdWithRequests() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        em.persist(requester);
        Request request = new Request()
                .setRequester(requester)
                .setStatus(RequestStatus.PENDING)
                .setCreated(date)
                .setEvent(EventMapper.mapFromEventFullDtoToEvent(eventFullDto));
        em.persist(request);
        List<ParticipationRequestDto> result = eventService.findRequestsByEventId(eventFullDto.getId(), initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(request.getId(), result.getFirst().getId());
    }


    @Test
    @DisplayName("Поиск requests по eventId и userId - неизвестный пользователь")
    void testFindRequestsByEventIdWrongUserId() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        try {
            eventService.findRequestsByEventId(eventFullDto.getId(), 999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск requests по eventId и userId - неизвестное событие")
    void testFindRequestsByEventIdWrongEventId() {
        em.persist(initiator);
        try {
            eventService.findRequestsByEventId(999L, initiator.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("Event with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск requests по eventId и userId - userId не инициатор")
    void testFindRequestsByEventIdUserNotInitiator() {
        em.persist(initiator);
        em.persist(initiator2);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        try {
            eventService.findRequestsByEventId(eventFullDto.getId(), initiator2.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Current user isn't an initiator of current event", e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление requests по eventId и userId - неизвестный пользователь")
    void testUpdateRequestsStatusWrongUserId() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest();
        try {
            eventService.updateRequestsStatus(dto, eventFullDto.getId(), 999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление requests по eventId и userId - неизвестное событие")
    void testUpdateRequestsStatusWrongEventId() {
        em.persist(initiator);
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest();
        try {
            eventService.updateRequestsStatus(dto, 999L, initiator.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("Event with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление requests по eventId и userId - userId не инициатор")
    void testUpdateRequestsStatusUserNotInitiator() {
        em.persist(initiator);
        em.persist(initiator2);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest();
        try {
            eventService.updateRequestsStatus(dto, eventFullDto.getId(), initiator2.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Current user isn't an initiator of current event", e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление requests по eventId и userId - нет запросов")
    void testUpdateRequestsStatusNoRequests() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        List<Long> requestIds = new ArrayList<>();
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        EventRequestStatusUpdateResult result = eventService.updateRequestsStatus(dto, eventFullDto.getId(), initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.getConfirmedRequests().size());
        Assertions.assertEquals(0, result.getRejectedRequests().size());
    }

    @Test
    @DisplayName("Обновление requests по eventId и userId - есть запрос - CONFIRMED")
    void testUpdateRequestsStatusWithRequestCONFIRMED() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        em.persist(requester);
        Request request = new Request()
                .setRequester(requester)
                .setStatus(RequestStatus.PENDING)
                .setCreated(date)
                .setEvent(EventMapper.mapFromEventFullDtoToEvent(eventFullDto));
        em.persist(request);
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(request.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        EventRequestStatusUpdateResult result = eventService.updateRequestsStatus(dto, eventFullDto.getId(), initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getConfirmedRequests().size());
        Assertions.assertEquals(0, result.getRejectedRequests().size());
    }

    @Test
    @DisplayName("Обновление requests по eventId и userId - есть запрос - REJECTED")
    void testUpdateRequestsStatusWithRequestREJECTED() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        newEventDto.setCategory(category.getId());
        EventFullDto eventFullDto = eventService.save(newEventDto, initiator.getId());
        em.persist(requester);
        Request request = new Request()
                .setRequester(requester)
                .setStatus(RequestStatus.PENDING)
                .setCreated(date)
                .setEvent(EventMapper.mapFromEventFullDtoToEvent(eventFullDto));
        em.persist(request);
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(request.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.REJECTED);
        EventRequestStatusUpdateResult result = eventService.updateRequestsStatus(dto, eventFullDto.getId(), initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.getConfirmedRequests().size());
        Assertions.assertEquals(1, result.getRejectedRequests().size());
    }
}
