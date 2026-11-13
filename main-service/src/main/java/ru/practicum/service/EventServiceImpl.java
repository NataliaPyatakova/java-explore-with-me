package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatClient;
import ru.practicum.dto.ViewStats;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enumeration.AdminEventStateAction;
import ru.practicum.enumeration.EventState;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.EventSpecification;
import ru.practicum.repository.LocationRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RequestService requestService;
    private final LocationRepository locationRepository;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final StatClient statClient;

    @Override
    @Transactional
    public EventFullDto save(NewEventDto newEventDto, Long userId) {
        log.info("save event newEventDto {}", newEventDto);
        //дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("For the requested operation the conditions are not met.",
                    "Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: "
                            + newEventDto.getEventDate().format(dtf));
        }
        User user = UserMapper.mapToUserFromUserDto(userService.findById(userId));
        Category category = CategoryMapper.mapToCategoryFromCategoryDto(categoryService.findById(newEventDto.getCategory()));
        Location location = locationRepository.save(LocationMapper.mapToLocationFromLocationDto(newEventDto.getLocation()));
        ParamEventSave paramEventSave = new ParamEventSave(user, category, location);
        Event event = EventMapper.mapFromNewEventDtoToEvent(newEventDto, paramEventSave);
        //у создаваемого события нет ни просмотров ни заявок
        ParamEventDto params = new ParamEventDto(0L, 0L);
        return EventMapper.mapFromEventToEventFullDto(eventRepository.save(event), params);
    }

    @Override
    @Transactional
    public EventFullDto updateByInitiator(UpdateEventUserRequest updateDto, Long eventId, Long userId) {
        log.info("updateByInitiator event updateDto {} eventId {} userId {}", updateDto, eventId, userId);
        ParamEventUpdate params = getParamEventUpdate(updateDto, eventId, userId);
        //изменить можно только отмененные события или события в состоянии ожидания модерации
        if (params.getEvent().getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Field: state. Error: изменить можно только отмененные события или события в " +
                    "состоянии ожидания модерации. Value: " + params.getEvent().getState(),
                    "For the requested operation the conditions are not met.");
        }
        //дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
        if (updateDto.getEventDate() != null) {
            if (updateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: "
                        + updateDto.getEventDate().format(dtf),
                        "For the requested operation the conditions are not met.");
            }
        }
        //текущий пользователь должен быть инициатором события
        if (!params.getEvent().getInitiator().getId().equals(userId)) {
            throw new ValidationException("Current user isn't an initiator of current event",
                    "For the requested operation the conditions are not met.");
        }
        //непонятно нужны ли просмотры и кол-во запросов при ответе на апдейт - пока отдаем
        List<Long> ids = new ArrayList<>();
        ids.add(eventId);
        LocalDateTime start = params.getEvent().getCreatedOn(); //читаем статистику просмотров с момента создания события
        Map<Long, ParamEventDto> mapParams = calcViewsAndRequests(ids, start);
        Event event = EventMapper.mapFromUpdateEventUserRequestToEvent(updateDto, params);
        return EventMapper.mapFromEventToEventFullDto(eventRepository.save(event), mapParams.get(eventId));
    }

    @Override
    @Transactional
    public EventFullDto updateByAdmin(UpdateEventAdminRequest updateDto, Long eventId) {
        log.info("updateByAdmin event updateDto {} eventId {}", updateDto, eventId);
        ParamEventUpdate params = getParamEventUpdate(updateDto, eventId, null);
        // дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
        if (updateDto.getEventDate() != null) {
            if (updateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new BadRequestException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: "
                        + updateDto.getEventDate().format(dtf),
                        "For the requested operation the conditions are not met.");
            }
        }
        //событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)
        if (AdminEventStateAction.PUBLISH_EVENT.equals(updateDto.getStateAction())
                && !params.getEvent().getState().equals(EventState.PENDING)) {
            throw new ValidationException("Cannot publish the event because it's not in the right state: "
                    + params.getEvent().getState(),
                    "For the requested operation the conditions are not met.");
        }
        //событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)
        if (AdminEventStateAction.REJECT_EVENT.equals(updateDto.getStateAction())
                && params.getEvent().getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Cannot reject the event because it's not in the right state: "
                    + params.getEvent().getState(),
                    "For the requested operation the conditions are not met.");
        }
        //непонятно нужны ли просмотры и кол-во запросов при ответе на апдейт - пока отдаем
        List<Long> ids = new ArrayList<>();
        ids.add(eventId);
        LocalDateTime start = params.getEvent().getCreatedOn(); //читаем статистику просмотров с момента создания события
        Map<Long, ParamEventDto> mapParams = calcViewsAndRequests(ids, start);
        Event event = EventMapper.mapFromUpdateEventAdminRequestToEvent(updateDto, params);
        return EventMapper.mapFromEventToEventFullDto(eventRepository.save(event), mapParams.get(eventId));
    }

    @Override
    //для Private
    public EventFullDto findEventByIdAndByUserId(Long eventId, Long userId) {
        log.info("findEventByIdAndByUserId eventId {} userId {}", eventId, userId);
        userService.findById(userId);
        return findById(eventId);
    }

    @Override
    //для Private findEventByIdAndByUserId
    public EventFullDto findById(Long id) {
        log.info("findById event id {}", id);
        Event event = findEventById(id);
        //непонятно нужна ли статистика в Private controller - пока отдаем
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        LocalDateTime start = event.getCreatedOn(); //читаем статистику просмотров с момента создания события
        Map<Long, ParamEventDto> mapParams = calcViewsAndRequests(ids, start);
        return EventMapper.mapFromEventToEventFullDto(event, mapParams.get(id));
    }

    @Override
    //для Private
    public List<EventShortDto> findEventsByUserId(ParamEventFindByUser params) {
        log.info("findEventsByUserId params {}", params);
        userService.findById(params.getUserId());
        //в требованиях не нашла - сортируем по Id чтобы не кучей
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize(), sortById);
        Specification<Event> spec = Specification.where(null);
        spec = spec.and(EventSpecification.hasInitiatorId(params.getUserId()));
        List<Event> listEvents = eventRepository.findAll(spec, pageable).getContent();
        Map<Long, ParamEventDto> mapParams;
        if (!listEvents.isEmpty()) {
            //непонятно нужна ли статистика в Private controller - пока отдаем
            List<Long> ids = listEvents.stream().map(Event::getId).toList();
            //читаем статистику просмотров с момента самого раннего создания события
            LocalDateTime start = listEvents.stream().map(Event::getCreatedOn).min(Comparator.naturalOrder())
                    .orElse(LocalDateTime.now());
            mapParams = calcViewsAndRequests(ids, start);
        } else {
            mapParams = Collections.emptyMap();
        }
        return listEvents.stream().map(event ->
                EventMapper.mapFromEventToEventShortDto(event, mapParams.get(event.getId()))).toList();
    }

    @Override
    public List<EventFullDto> findEventsByAdmin(ParamEventsFindByAdmin params) {
        log.info("findEventsByAdmin params {}", params);
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize(), sortById);
        Specification<Event> spec = Specification.where(null);
        if (params.getUsers() != null) spec = spec.and(EventSpecification.hasInitiatorIdInList(params.getUsers()));
        if (params.getStates() != null) spec = spec.and(EventSpecification.hasStateInList(params.getStates()));
        if (params.getCategories() != null)
            spec = spec.and(EventSpecification.hasCategoryIdInList(params.getCategories()));
        if (params.getRangeStart() != null) spec = spec.and(EventSpecification.isAfter(params.getRangeStart()));
        if (params.getRangeEnd() != null) spec = spec.and(EventSpecification.isBefore(params.getRangeEnd()));
        List<Event> listEvents = eventRepository.findAll(spec, pageable).getContent();
        Map<Long, ParamEventDto> mapParams;
        if (!listEvents.isEmpty()) {
            //непонятно нужна ли статистика в admin controller - пока отдаем
            List<Long> ids = listEvents.stream().map(Event::getId).toList();
            //читаем статистику просмотров с момента самого раннего создания события
            LocalDateTime start = listEvents.stream().map(Event::getCreatedOn).min(Comparator.naturalOrder())
                    .orElse(LocalDateTime.now());
            mapParams = calcViewsAndRequests(ids, start);
        } else {
            mapParams = new HashMap<>();
        }
        return listEvents.stream().map(event ->
                EventMapper.mapFromEventToEventFullDto(event, mapParams.get(event.getId()))).toList();
    }

    @Override
    public EventFullDto findPublishedEventById(Long id) {
        log.info("findPublishedEventById id {}", id);
        //событие должно быть опубликовано
        Event event = eventRepository.findEventByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found",
                        "The required object was not found."));
        //информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        LocalDateTime start = event.getCreatedOn(); //читаем статистику просмотров с момента создания события
        Map<Long, ParamEventDto> mapParams = calcViewsAndRequests(ids, start);
        //информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
        return EventMapper.mapFromEventToEventFullDto(event, mapParams.get(id));
    }

    @Override
    public List<EventShortDto> findPublishedEventsByParam(ParamEventsFindByParam params) {
        log.info("findPublishedEventsByParam params {}", params);
        List<EventShortDto> result = new ArrayList<>();
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize());
        Specification<Event> spec = Specification.where(null);
        //в выдаче должны быть только опубликованные события
        spec = spec.and(EventSpecification.hasState(EventState.PUBLISHED));
        if (params.getText() != null && !params.getText().isEmpty()) {
            spec = spec.and(EventSpecification.hasTextFieldsLike(params.getText()));
        }
        if (params.getCategories() != null)
            spec = spec.and(EventSpecification.hasCategoryIdInList(params.getCategories()));
        if (params.getPaid() != null) spec = spec.and(EventSpecification.isPaid(params.getPaid()));
        //если в запросе не указан диапазон дат, то события, которые произойдут позже текущей даты и времени
        if (params.getRangeStart() == null && params.getRangeEnd() == null) {
            spec = spec.and(EventSpecification.isAfter(LocalDateTime.now()));
        }
        if (params.getRangeStart() != null) spec = spec.and(EventSpecification.isAfter(params.getRangeStart()));
        if (params.getRangeEnd() != null) spec = spec.and(EventSpecification.isBefore(params.getRangeEnd()));
        if (params.isOnlyAvailable()) spec = spec.and(EventSpecification.hasParticipantLimit(0));
        List<Event> listEvents = eventRepository.findAll(spec, pageable).getContent();
        Map<Long, ParamEventDto> mapParams;
        if (!listEvents.isEmpty()) {
            //информация о каждом событии должна включать в себя количество просмотров
            // и количество уже одобренных заявок на участие
            List<Long> ids = listEvents.stream().map(Event::getId).toList();
            //читаем статистику просмотров с момента самого раннего создания события
            LocalDateTime start = listEvents.stream().map(Event::getCreatedOn).min(Comparator.naturalOrder())
                    .orElse(LocalDateTime.now());
            mapParams = calcViewsAndRequests(ids, start);
        } else {
            mapParams = new HashMap<>();
        }
        List<EventShortDto> listEventsShort = listEvents.stream().map(event ->
                EventMapper.mapFromEventToEventShortDto(event, mapParams.get(event.getId()))).toList();
        //сортировка
        if (params.getSort() != null) {
            if (params.getSort().equals(ru.practicum.enumeration.Sort.VIEWS)) { //либо по количеству просмотров
                result = listEventsShort.stream().sorted(Comparator.comparing(EventShortDto::getViews)).toList();
            }
            if (params.getSort().equals(ru.practicum.enumeration.Sort.EVENT_DATE)) { //либо по датам событий
                result = listEventsShort.stream().sorted(Comparator.comparing(EventShortDto::getEventDate)).toList();
            }
        } else {
            result = listEventsShort;
        }
        return result;
    }

    @Override
    public List<ParticipationRequestDto> findRequestsByEventId(Long eventId, Long userId) {
        log.info("findRequestsByEventId eventId {} userId {}", eventId, userId);
        Event foundedEvent = findEventById(eventId);
        userService.findById(userId);
        if (!foundedEvent.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Current user isn't an initiator of current event",
                    "For the requested operation the conditions are not met.");
        }
        return requestService.findByEventId(eventId);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(EventRequestStatusUpdateRequest dto,
                                                               Long eventId,
                                                               Long userId) {
        log.info("updateRequestsStatus eventId {} userId {}", eventId, userId);
        Event event = findEventById(eventId);
        userService.findById(userId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ValidationException("Current user isn't an initiator of current event",
                    "For the requested operation the conditions are not met.");
        }
        return requestService.updateRequestsStatus(dto, event);
    }

    @Override
    //для CompilationService getEvents
    public List<EventShortDto> findAllByIdIn(List<Long> ids) {
        log.info("findAllByIdIn event ids {}", ids);
        List<Event> events = eventRepository.findAllById(ids);
        Map<Long, ParamEventDto> mapParams;
        if (!events.isEmpty()) {
            //непонятно нужна ли статистика в CompilationService - пока отдаем
            //читаем статистику просмотров с момента самого раннего создания события
            LocalDateTime start = events.stream().map(Event::getCreatedOn).min(Comparator.naturalOrder()).orElse(LocalDateTime.now());
            mapParams = calcViewsAndRequests(ids, start);
        } else {
            mapParams = new HashMap<>();
        }
        return events.stream().map(event ->
                EventMapper.mapFromEventToEventShortDto(event, mapParams.get(event.getId()))).toList();
    }

    @Override
    public Map<Long, ParamEventDto> calcViewsAndRequests(List<Long> ids, LocalDateTime start) {
        log.info("calcViewsAndRequests ids {}", ids);
        Map<Long, ParamEventDto> mapParams = new HashMap<>();
        ids.forEach(id -> mapParams.putIfAbsent(id, new ParamEventDto(0L, 0L)));
        //confirmed Requests
        List<EventCountRequests> listCountRequest = requestService.countConfirmedRequestsByListEventId(ids);
        listCountRequest.forEach(count ->
                mapParams.replace(count.getId(), new ParamEventDto(count.getCount(), 0L)));
        //views
        Map<String, Long> mapURI = new HashMap<>();
        ids.forEach(id -> mapURI.putIfAbsent("/events/" + id, id));
        List<String> uris = ids.stream().map(id -> "/events/" + id).toList();
        LocalDateTime end = LocalDateTime.now(); //по текущий момент
        List<ViewStats> listViews = statClient.getStats(start, end, uris, true);
        listViews.forEach(viewStats -> {
            Long eventId = mapURI.get(viewStats.getUri());
            mapParams.replace(eventId, new ParamEventDto(mapParams.get(eventId).getConfirmedRequests(), viewStats.getHits()));
        });
        return mapParams;
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found",
                        "The required object was not found."));
    }

    private <T extends UpdateEventRequest> ParamEventUpdate getParamEventUpdate(T updateDto, Long eventId, Long userId) {
        if (userId != null) {
            userService.findById(userId);
        }
        ParamEventUpdate params = new ParamEventUpdate();
        Event foundedEvent = findEventById(eventId);
        params.setEvent(foundedEvent);
        if (updateDto.getCategory() != null) {
            Category category = CategoryMapper.mapToCategoryFromCategoryDto(categoryService.findById(updateDto.getCategory()));
            params.setCategory(category);
        }
        if (updateDto.getLocation() != null) {
            Location location = locationRepository.save(LocationMapper.mapToLocationFromLocationDto(updateDto.getLocation()));
            params.setLocation(location);
        }
        return params;
    }
}
