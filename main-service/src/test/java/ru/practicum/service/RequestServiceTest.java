package ru.practicum.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enumeration.EventState;
import ru.practicum.enumeration.RequestStatus;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RequestServiceTest {

    @Autowired
    private RequestService requestService;
    private final EntityManager em;

    private final LocalDateTime date = LocalDateTime.now().plusDays(1);
    private final LocalDateTime now = LocalDateTime.now();
    private final Category category = new Category().setName("test_category");
    private final Location location = new Location().setLat(15.15).setLon(16.16);
    private final User requester = new User()
            .setEmail("requester_email")
            .setName("requester_name");
    private final User requester2 = new User()
            .setEmail("requester_email2")
            .setName("requester_name2");
    private final User requester3 = new User()
            .setEmail("requester_email3")
            .setName("requester_name3");
    private final User initiator = new User()
            .setEmail("email_initiator@")
            .setName("initiator_name");
    private final Event event = new Event()
            .setTitle("test_title")
            .setDescription("test_description")
            .setAnnotation("test_annotation")
            .setEventDate(date)
            .setPaid(true)
            .setParticipantLimit(0)
            .setRequestModeration(true)
            .setCreatedOn(now)
            .setState(EventState.PUBLISHED);
    private final Event event2 = new Event()
            .setTitle("test_title2")
            .setDescription("test_description2")
            .setAnnotation("test_annotation2")
            .setEventDate(date)
            .setPaid(true)
            .setParticipantLimit(0)
            .setRequestModeration(true)
            .setCreatedOn(now)
            .setState(EventState.PUBLISHED);


    @Test
    @DisplayName("Сохранение request - все хорошо")
    public void testSave() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        ParticipationRequestDto result = requestService.save(requester.getId(), event.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(requester.getId(), result.getRequester());
        Assertions.assertEquals(event.getId(), result.getEvent());
        Assertions.assertEquals(RequestStatus.PENDING, result.getStatus());
        Assertions.assertNotNull(result.getCreated());
    }

    @Test
    @DisplayName("Сохранение request - неверный user")
    public void testSaveWrongUser() {
        try {
            requestService.save(999L, event.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Сохранение request - неверный event")
    public void testSaveWrongEvent() {
        em.persist(requester);
        try {
            requestService.save(requester.getId(), 999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Event with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Сохранение request - инициатор события добавил запрос")
    public void testSaveInitiatorRequest() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        try {
            requestService.save(initiator.getId(), event.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Validation Error", e.getMessage());
            Assertions.assertEquals("Initiator of event could not make request for participation", e.getReason());
        }
    }

    @Test
    @DisplayName("Сохранение request - отмененное событие")
    public void testSaveNotPublishedEvent() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setState(EventState.CANCELED);
        em.persist(event);
        try {
            requestService.save(requester.getId(), event.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Validation Error", e.getMessage());
            Assertions.assertEquals("Event wasn't published", e.getReason());
        }
    }

    @Test
    @DisplayName("Сохранение request - ожидающее событие")
    public void testSavePendingEvent() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setState(EventState.PENDING);
        em.persist(event);
        try {
            requestService.save(requester.getId(), event.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("Validation Error", e.getMessage());
            Assertions.assertEquals("Event wasn't published", e.getReason());
        }
    }

    @Test
    @DisplayName("Сохранение request - есть лимит, но нет одобренных запросов")
    public void testSaveOverLimitNoConfirmed() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setParticipantLimit(1);
        em.persist(event);
        //сохранили один нормальный запрос
        requestService.save(requester.getId(), event.getId());
        //пытаемся впихнуть еще один запрос
        ParticipationRequestDto result = requestService.save(requester2.getId(), event.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(requester2.getId(), result.getRequester());
        Assertions.assertEquals(event.getId(), result.getEvent());
        Assertions.assertEquals(RequestStatus.PENDING, result.getStatus());
        Assertions.assertNotNull(result.getCreated());
    }

    @Test
    @DisplayName("Сохранение request - превышен лимит запросов")
    public void testSaveOverLimitConfirmed() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setParticipantLimit(1);
        em.persist(event);
        //сохранили один нормальный запрос
        ParticipationRequestDto result = requestService.save(requester.getId(), event.getId());
        //проставляем ему статус подтвержден
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(result.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        requestService.updateRequestsStatus(dto, event);
        try {
            //пытаемся впихнуть еще один запрос
            requestService.save(requester2.getId(), event.getId());
        } catch (ValidationException e) {
            Assertions.assertEquals("The participant limit has been reached", e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление request - все хорошо")
    public void testUpdate() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        ParticipationRequestDto result = requestService.update(requester.getId(), requestDto.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(requester.getId(), result.getRequester());
        Assertions.assertEquals(event.getId(), result.getEvent());
        Assertions.assertEquals(RequestStatus.CANCELED, result.getStatus());
        Assertions.assertNotNull(result.getCreated());
    }

    @Test
    @DisplayName("Обновление request - неверный user")
    public void testUpdateWrongUser() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        try {
            requestService.update(999L, requestDto.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление request - неверный request")
    public void testUpdateWrongRequest() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        requestService.save(requester.getId(), event.getId());
        try {
            requestService.update(requester.getId(), 999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Request with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск по requesterId - все хорошо")
    public void testFindByUserId() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        ParticipationRequestDto participationRequestDto = requestService.save(requester.getId(), event.getId());
        List<ParticipationRequestDto> result = requestService.findByUserId(requester.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(participationRequestDto.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("Поиск по requesterId - нет запросов")
    public void testFindByUserIdNoRequest() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        requestService.save(requester.getId(), event.getId());
        List<ParticipationRequestDto> result = requestService.findByUserId(initiator.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск по requesterId - неверный user")
    public void testFindByUserIdWrongUser() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        requestService.save(requester.getId(), event.getId());
        try {
            requestService.findByUserId(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск по eventId - все хорошо")
    public void testFindByEventId() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        ParticipationRequestDto participationRequestDto = requestService.save(requester.getId(), event.getId());
        List<ParticipationRequestDto> result = requestService.findByEventId(event.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(participationRequestDto.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("Поиск по eventId - нет запросов")
    public void testFindByEventIdNoRequest() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        requestService.save(requester.getId(), event.getId());
        event2.setInitiator(initiator);
        event2.setCategory(category);
        event2.setLocation(location);
        em.persist(event2);
        List<ParticipationRequestDto> result = requestService.findByEventId(event2.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск по eventId - неверный event")
    public void testFindByEventIdWrongEvent() {
        em.persist(requester);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        requestService.save(requester.getId(), event.getId());
        try {
            requestService.findByEventId(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Event with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление статусов request confirmed - все хорошо")
    public void testUpdateRequestsStatusConfirmed() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        //сохранили один нормальный запрос
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        //проставляем ему статус подтвержден
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(dto, event);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getConfirmedRequests().size());
        Assertions.assertEquals(0, result.getRejectedRequests().size());
        Assertions.assertEquals(requestDto.getId(), result.getConfirmedRequests().getFirst().getId());
        //проверим сам статус
        List<ParticipationRequestDto> requestDtoList = requestService.findByEventId(event.getId());
        Assertions.assertNotNull(requestDtoList);
        Assertions.assertEquals(requestDto.getId(), requestDtoList.getFirst().getId());
        Assertions.assertEquals(RequestStatus.CONFIRMED, requestDtoList.getFirst().getStatus());
    }

    @Test
    @DisplayName("Обновление статусов request reject - все хорошо")
    public void testUpdateRequestsStatusRejected() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setParticipantLimit(1);
        em.persist(event);
        //сохранили один нормальный запрос
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        //проставляем ему статус подтвержден
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.REJECTED);
        EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(dto, event);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.getConfirmedRequests().size());
        Assertions.assertEquals(1, result.getRejectedRequests().size());
        Assertions.assertEquals(requestDto.getId(), result.getRejectedRequests().getFirst().getId());
        //проверим сам статус
        List<ParticipationRequestDto> requestDtoList = requestService.findByEventId(event.getId());
        Assertions.assertNotNull(requestDtoList);
        Assertions.assertEquals(requestDto.getId(), requestDtoList.getFirst().getId());
        Assertions.assertEquals(RequestStatus.REJECTED, requestDtoList.getFirst().getStatus());
    }

    @Test
    @DisplayName("Обновление статусов request - неверное состояние запроса")
    public void testUpdateRequestsStatusWrongStatus() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        //сохранили один нормальный запрос
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        //проставляем ему статус подтвержден
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(dto, event);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getConfirmedRequests().size());
        Assertions.assertEquals(0, result.getRejectedRequests().size());
        Assertions.assertEquals(requestDto.getId(), result.getConfirmedRequests().getFirst().getId());
        //а потом ему же пытаемся проставить статус отклонен
        dto.setStatus(RequestStatus.REJECTED);
        try {
            requestService.updateRequestsStatus(dto, event);
        } catch (BadRequestException e) {
            Assertions.assertEquals("Request must have status PENDING", e.getMessage());
            Assertions.assertEquals("Incorrectly made request.", e.getReason());
            //проверим сам статус - он не должен был измениться
            List<ParticipationRequestDto> requestDtoList = requestService.findByEventId(event.getId());
            Assertions.assertNotNull(requestDtoList);
            Assertions.assertEquals(requestDto.getId(), requestDtoList.getFirst().getId());
            Assertions.assertEquals(RequestStatus.CONFIRMED, requestDtoList.getFirst().getStatus());
        }
    }

    @Test
    @DisplayName("Обновление статусов request - неверное состояние запроса только у одного из списка")
    public void testUpdateRequestsStatusWrongStatusOnlyOne() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        //сохранили запросы
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        ParticipationRequestDto requestDto2 = requestService.save(requester2.getId(), event.getId());
        //проставляем ему статус подтвержден
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(dto, event);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getConfirmedRequests().size());
        Assertions.assertEquals(0, result.getRejectedRequests().size());
        Assertions.assertEquals(requestDto.getId(), result.getConfirmedRequests().getFirst().getId());
        //а потом им двоим  же пытаемся проставить статус отклонен
        requestIds.add(requestDto2.getId());
        dto.setStatus(RequestStatus.REJECTED);
        try {
            requestService.updateRequestsStatus(dto, event);
        } catch (BadRequestException e) {
            Assertions.assertEquals("Request must have status PENDING", e.getMessage());
            Assertions.assertEquals("Incorrectly made request.", e.getReason());
            //проверим сам статус - он не должен был измениться
            List<ParticipationRequestDto> requestDtoList = requestService.findByEventId(event.getId());
            Assertions.assertNotNull(requestDtoList);
            Assertions.assertEquals(requestDto.getId(), requestDtoList.getFirst().getId());
            Assertions.assertEquals(RequestStatus.CONFIRMED, requestDtoList.getFirst().getStatus());
            Assertions.assertEquals(requestDto2.getId(), requestDtoList.getLast().getId());
            Assertions.assertEquals(RequestStatus.PENDING, requestDtoList.getLast().getStatus());
        }
    }

    @Test
    @DisplayName("Обновление статусов request - неверное событие только у одного из списка")
    public void testUpdateRequestsStatusWrongEventOnlyOne() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        event2.setInitiator(initiator);
        event2.setCategory(category);
        event2.setLocation(location);
        em.persist(event2);
        //сохранили запросы
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        ParticipationRequestDto requestDto2 = requestService.save(requester2.getId(), event2.getId());
        //проставляем ему статус подтвержден
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        requestIds.add(requestDto2.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        try {
            requestService.updateRequestsStatus(dto, event);
        } catch (BadRequestException e) {
            Assertions.assertEquals("Requests must belong to current Event", e.getMessage());
            Assertions.assertEquals("Incorrectly made request.", e.getReason());
            //проверим сам статус - он не должен был измениться
            //событие раз
            List<ParticipationRequestDto> requestDtoList = requestService.findByEventId(event.getId());
            Assertions.assertEquals(requestDto.getId(), requestDtoList.getFirst().getId());
            Assertions.assertEquals(RequestStatus.PENDING, requestDtoList.getFirst().getStatus());
            //второй запрос под другим событием
            requestDtoList = requestService.findByEventId(event2.getId());
            Assertions.assertEquals(requestDto2.getId(), requestDtoList.getLast().getId());
            Assertions.assertEquals(RequestStatus.PENDING, requestDtoList.getLast().getStatus());
        }
    }

    @Test
    @DisplayName("Обновление статусов request confirmed - лимит у события есть. но подтвержденных запросов нет")
    public void testUpdateRequestsStatusConfirmedInTheLimit() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setParticipantLimit(1);
        em.persist(event);
        //сохранили один нормальный запрос
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        //проставляем ему статус подтвержден
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(dto, event);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getConfirmedRequests().size());
        Assertions.assertEquals(0, result.getRejectedRequests().size());
        Assertions.assertEquals(requestDto.getId(), result.getConfirmedRequests().getFirst().getId());
        //проверим сам статус
        List<ParticipationRequestDto> requestDtoList = requestService.findByEventId(event.getId());
        Assertions.assertNotNull(requestDtoList);
        Assertions.assertEquals(requestDto.getId(), requestDtoList.getFirst().getId());
        Assertions.assertEquals(RequestStatus.CONFIRMED, requestDtoList.getFirst().getStatus());
    }

    @Test
    @DisplayName("Обновление статусов request confirmed - лимит у события есть, есть подтвержденные запросы, но мы влезаем")
    public void testUpdateRequestsStatusConfirmedInTheLimitWithConfirmedRequest() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setParticipantLimit(2);
        em.persist(event);
        //сохранили один нормальный запрос
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        //проставляем ему статус подтвержден
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(dto, event);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getConfirmedRequests().size());
        Assertions.assertEquals(0, result.getRejectedRequests().size());
        Assertions.assertEquals(requestDto.getId(), result.getConfirmedRequests().getFirst().getId());
        //проверим сам статус
        List<ParticipationRequestDto> requestDtoList = requestService.findByEventId(event.getId());
        Assertions.assertNotNull(requestDtoList);
        Assertions.assertEquals(requestDto.getId(), requestDtoList.getFirst().getId());
        Assertions.assertEquals(RequestStatus.CONFIRMED, requestDtoList.getFirst().getStatus());
        //а теперь еще один запрос
        ParticipationRequestDto requestDto2 = requestService.save(requester2.getId(), event.getId());
        List<Long> requestIds2 = new ArrayList<>();
        requestIds2.add(requestDto2.getId());
        EventRequestStatusUpdateRequest dto2 = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds2)
                .setStatus(RequestStatus.CONFIRMED);
        EventRequestStatusUpdateResult result2 = requestService.updateRequestsStatus(dto2, event);
        Assertions.assertNotNull(result2);
        Assertions.assertEquals(1, result2.getConfirmedRequests().size());
        Assertions.assertEquals(0, result2.getRejectedRequests().size());
        Assertions.assertEquals(requestDto2.getId(), result2.getConfirmedRequests().getFirst().getId());
        //проверим сам статус
        List<ParticipationRequestDto> requestDtoList2 = requestService.findByUserId(requester2.getId());
        Assertions.assertNotNull(requestDtoList2);
        Assertions.assertEquals(requestDto2.getId(), requestDtoList2.getFirst().getId());
        Assertions.assertEquals(RequestStatus.CONFIRMED, requestDtoList2.getFirst().getStatus());
    }

    @Test
    @DisplayName("Обновление статусов request confirmed - лимит у события есть, нет подтвержденные запросы, но мы влезаем")
    public void testUpdateRequestsStatusConfirmedInTheLimitNoConfirmedRequest() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setParticipantLimit(2);
        em.persist(event);
        //сохранили один нормальный запрос
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        ParticipationRequestDto requestDto2 = requestService.save(requester2.getId(), event.getId());
        //проставляем ему статус подтвержден
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        requestIds.add(requestDto2.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(dto, event);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.getConfirmedRequests().size());
        Assertions.assertEquals(0, result.getRejectedRequests().size());
        Assertions.assertEquals(requestDto.getId(), result.getConfirmedRequests().getFirst().getId());
        Assertions.assertEquals(requestDto2.getId(), result.getConfirmedRequests().getLast().getId());
        //проверим сам статус у обоих заявок
        List<ParticipationRequestDto> requestDtoList = requestService.findByEventId(event.getId());
        Assertions.assertNotNull(requestDtoList);
        Assertions.assertEquals(2, requestDtoList.size());
        Assertions.assertEquals(requestDto.getId(), requestDtoList.getFirst().getId());
        Assertions.assertEquals(RequestStatus.CONFIRMED, requestDtoList.getFirst().getStatus());
        Assertions.assertEquals(requestDto2.getId(), requestDtoList.getLast().getId());
        Assertions.assertEquals(RequestStatus.CONFIRMED, requestDtoList.getLast().getStatus());
    }

    @Test
    @DisplayName("Обновление статусов request confirmed - лимит у события есть, есть подтвержденные запросы, мы не влезаем")
    public void testUpdateRequestsStatusConfirmedOverTheLimitWithConfirmedRequest() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setParticipantLimit(1);
        em.persist(event);
        //сохранили один нормальный запрос
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        //а теперь еще один запрос - сразу до того как лимит переполнился
        ParticipationRequestDto requestDto2 = requestService.save(requester2.getId(), event.getId());
        //проставляем ему статус подтвержден первому запросу
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(dto, event);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getConfirmedRequests().size());
        Assertions.assertEquals(0, result.getRejectedRequests().size());
        Assertions.assertEquals(requestDto.getId(), result.getConfirmedRequests().getFirst().getId());
        //проверим сам статус
        List<ParticipationRequestDto> requestDtoList = requestService.findByEventId(event.getId());
        Assertions.assertNotNull(requestDtoList);
        Assertions.assertEquals(requestDto.getId(), requestDtoList.getFirst().getId());
        Assertions.assertEquals(RequestStatus.CONFIRMED, requestDtoList.getFirst().getStatus());
        //а теперь попробуем и второй апнуть
        List<Long> requestIds2 = new ArrayList<>();
        requestIds2.add(requestDto2.getId());
        EventRequestStatusUpdateRequest dto2 = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds2)
                .setStatus(RequestStatus.CONFIRMED);
        try {
            requestService.updateRequestsStatus(dto2, event);
        } catch (ValidationException e) {
            Assertions.assertEquals("The participant limit has been reached", e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
            //проверим сам статус - он не должен был измениться
            //событие раз
            List<ParticipationRequestDto> requestDtoList2 = requestService.findByUserId(requester2.getId());
            Assertions.assertEquals(requestDto2.getId(), requestDtoList2.getFirst().getId());
            Assertions.assertEquals(RequestStatus.PENDING, requestDtoList2.getFirst().getStatus());
        }
    }

    @Test
    @DisplayName("Обновление статусов request confirmed - лимит у события есть, нет подтвержденных запросы, мы не влезаем")
    public void testUpdateRequestsStatusConfirmedOverTheLimitWithNoConfirmedRequest() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setParticipantLimit(1);
        em.persist(event);
        //сохранили один нормальный запрос
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        //а теперь еще один запрос
        ParticipationRequestDto requestDto2 = requestService.save(requester2.getId(), event.getId());
        //проставляем ему статус подтвержден обоим сразу - мы не влезем в лимит
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        requestIds.add(requestDto2.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        try {
            requestService.updateRequestsStatus(dto, event);
        } catch (ValidationException e) {
            Assertions.assertEquals("The participant limit has been reached", e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
            //проверим сам статус - он не должен был измениться
            //событие раз
            List<ParticipationRequestDto> requestDtoList = requestService.findByUserId(requester.getId());
            Assertions.assertEquals(requestDto.getId(), requestDtoList.getFirst().getId());
            Assertions.assertEquals(RequestStatus.PENDING, requestDtoList.getFirst().getStatus());
            //событие два
            List<ParticipationRequestDto> requestDtoList2 = requestService.findByUserId(requester2.getId());
            Assertions.assertEquals(requestDto2.getId(), requestDtoList2.getFirst().getId());
            Assertions.assertEquals(RequestStatus.PENDING, requestDtoList2.getFirst().getStatus());
        }
    }

    @Test
    @DisplayName("Обновление статусов request confirmed - лимит у события есть, есть подтвержденных запросы, мы не влезаем")
    public void testUpdateRequestsStatusConfirmedOverTheLimitWithConfirmedRequest2() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(requester3);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setParticipantLimit(2);
        em.persist(event);
        //сохранили один нормальный запрос
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        //а теперь еще один запрос
        ParticipationRequestDto requestDto2 = requestService.save(requester2.getId(), event.getId());
        //b еще один запрос
        ParticipationRequestDto requestDto3 = requestService.save(requester3.getId(), event.getId());
        //проставляем статус подтвержден первому
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.CONFIRMED);
        //один проходит
        requestService.updateRequestsStatus(dto, event);
        try {
            List<Long> requestIds2 = new ArrayList<>();
            requestIds2.add(requestDto2.getId());
            requestIds2.add(requestDto3.getId());
            EventRequestStatusUpdateRequest dto2 = new EventRequestStatusUpdateRequest()
                    .setRequestIds(requestIds2)
                    .setStatus(RequestStatus.CONFIRMED);
            //плюс еще два - не проходят
            requestService.updateRequestsStatus(dto2, event);
        } catch (ValidationException e) {
            Assertions.assertEquals("The participant limit has been reached", e.getMessage());
            Assertions.assertEquals("For the requested operation the conditions are not met.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление статусов request rejected - лимит у события есть, есть подтвержденных запросы, " +
            "мы не влезаем - Все проходит")
    public void testUpdateRequestsStatusRejectedOverTheLimitWithConfirmedRequest2() {
        em.persist(requester);
        em.persist(requester2);
        em.persist(requester3);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        event.setParticipantLimit(2);
        em.persist(event);
        //сохранили один нормальный запрос
        ParticipationRequestDto requestDto = requestService.save(requester.getId(), event.getId());
        //а теперь еще один запрос
        ParticipationRequestDto requestDto2 = requestService.save(requester2.getId(), event.getId());
        //И еще один запрос
        ParticipationRequestDto requestDto3 = requestService.save(requester3.getId(), event.getId());
        //проставляем статус подтвержден первому
        List<Long> requestIds = new ArrayList<>();
        requestIds.add(requestDto.getId());
        requestIds.add(requestDto2.getId());
        requestIds.add(requestDto3.getId());
        EventRequestStatusUpdateRequest dto = new EventRequestStatusUpdateRequest()
                .setRequestIds(requestIds)
                .setStatus(RequestStatus.REJECTED);
        //Все проходят - при отклонении лимиты не проверяем
        EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(dto, event);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.getConfirmedRequests().size());
        Assertions.assertEquals(3, result.getRejectedRequests().size());
    }
}