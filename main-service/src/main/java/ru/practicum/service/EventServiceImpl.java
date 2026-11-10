package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enumeration.AdminEventStateAction;
import ru.practicum.enumeration.EventState;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public EventFullDto save(NewEventDto newEventDto, Long userId) {
        log.info("save event newEventDto {}", newEventDto);
        //дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("For the requested operation the conditions are not met.",
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
    public EventFullDto updateByInitiator(UpdateEventUserRequest updateDto, Long eventId, Long userId) {
        log.info("updateByInitiator event updateDto {} eventId {} userId {}", updateDto, eventId, userId);
        Event foundedEvent = findEventById(eventId);
        User user = UserMapper.mapToUserFromUserDto(userService.findById(userId));
        //TODO проверить на пустой категории в Юнит тестировании
        Category category = CategoryMapper.mapToCategoryFromCategoryDto(categoryService.findById(updateDto.getCategory()));
        //изменить можно только отмененные события или события в состоянии ожидания модерации
        if (foundedEvent.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Field: state. Error: изменить можно только отмененные события или события в " +
                    "состоянии ожидания модерации. Value: " + foundedEvent.getState(),
                    "For the requested operation the conditions are not met.");
        }
        //дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента
        if (updateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ValidationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: "
                    + updateDto.getEventDate().format(dtf),
                    "For the requested operation the conditions are not met.");
        }
        //текущий пользователь должен быть инициатором события
        if (foundedEvent.getInitiator().getId().equals(user.getId())) {
            throw new ValidationException("Current user isn't an initiator of current event",
                    "For the requested operation the conditions are not met.");
        }
        //TODO обработать NewEventState stateAction после тестирования постман
        List<Long> ids = new ArrayList<>();
        ids.add(eventId);
        Map<Long, ParamEventDto> mapParams = calcStatistic(ids);
        ParamEventUpdate params = new ParamEventUpdate().setCategory(category);
        Event event = EventMapper.mapFromUpdateEventUserRequestToEvent(foundedEvent, updateDto, params);
        return EventMapper.mapFromEventToEventFullDto(eventRepository.save(event), mapParams.get(eventId));
    }

    @Override
    public EventFullDto updateByAdmin(UpdateEventAdminRequest updateDto, Long eventId) {
        log.info("updateByAdmin event updateDto {} eventId {}", updateDto, eventId);
        Event foundedEvent = findEventById(eventId);
        //TODO проверить на пустой категории в Юнит тестировании
        Category category = CategoryMapper.mapToCategoryFromCategoryDto(categoryService.findById(updateDto.getCategory()));
        // дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)
        if (updateDto.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new ValidationException("Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value: "
                    + updateDto.getEventDate().format(dtf),
                    "For the requested operation the conditions are not met.");
        }
        //событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)
        if (AdminEventStateAction.PUBLISH_EVENT.equals(updateDto.getStateAction())
                && !foundedEvent.getState().equals(EventState.PENDING)) {
            throw new ValidationException("Cannot publish the event because it's not in the right state: "
                    + foundedEvent.getState(),
                    "For the requested operation the conditions are not met.");
        }
        //событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)
        if (AdminEventStateAction.REJECT_EVENT.equals(updateDto.getStateAction())
                && foundedEvent.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Cannot reject the event because it's not in the right state: "
                    + foundedEvent.getState(),
                    "For the requested operation the conditions are not met.");
        }
        List<Long> ids = new ArrayList<>();
        ids.add(eventId);
        Map<Long, ParamEventDto> mapParams = calcStatistic(ids);
        ParamEventUpdate params = new ParamEventUpdate().setCategory(category);
        Event event = EventMapper.mapFromUpdateEventAdminRequestToEvent(foundedEvent, updateDto, params);
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
        List<Long> ids = new ArrayList<>();
        ids.add(id);
        //TODO непонятно нужна ли статистика в Private controller
        Map<Long, ParamEventDto> mapParams = calcStatistic(ids);
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
        Page<Event> pageEvents = eventRepository.findAll(spec, pageable);
        List<Event> listEvents = pageEvents.getContent();
        //TODO непонятно нужна ли статистика в Private controller
        List<Long> ids = new ArrayList<>();
        listEvents.forEach(event -> ids.add(event.getId()));
        Map<Long, ParamEventDto> mapParams = calcStatistic(ids);
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
        Page<Event> pageEvents = eventRepository.findAll(spec, pageable);
        List<Event> listEvents = pageEvents.getContent();
        //TODO непонятно нужна ли статистика в admin controller
        List<Long> ids = new ArrayList<>();
        listEvents.forEach(event -> ids.add(event.getId()));
        Map<Long, ParamEventDto> mapParams = calcStatistic(ids);
        return listEvents.stream().map(event ->
                EventMapper.mapFromEventToEventFullDto(event, mapParams.get(event.getId()))).toList();
    }

    @Override
    public EventFullDto findPublishedEventById(Long id) {
        //TODO написать метод
        /*
        событие должно быть опубликовано
информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов
информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики
         */
        return null;
    }

    @Override
    public List<EventShortDto> findPublishedEventsByParam(ParamEventsFindByParam params) {
        //TODO написать метод
        /*
        это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события
текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв
если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события, которые произойдут позже текущей даты и времени
информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на участие
информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики

сортировка списка событий должна быть организована
либо по количеству просмотров, которое будет запрашиваться в сервисе статистики, либо по датам событий;
         */
        return List.of();
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
        //если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
        //TODO посмотреть как это ожидается в тестах постман
        return requestService.updateRequestsStatus(dto, event);
    }

    @Override
    //для CompilationService getEvents
    public List<EventShortDto> findAllByIdIn(List<Long> ids) {
        log.info("findAllByIdIn event ids {}", ids);
        List<Event> events = eventRepository.findAllById(ids);
        //TODO непонятно нужна ли статистика в CompilationService
        Map<Long, ParamEventDto> mapParams = calcStatistic(ids);
        return events.stream().map(event ->
                EventMapper.mapFromEventToEventShortDto(event, mapParams.get(event.getId()))).toList();
    }

    @Override
    public Map<Long, ParamEventDto> calcStatistic(List<Long> ids) {
        log.info("calcStatistic ids {}", ids);
        //TODO дописать статистику
        Map<Long, ParamEventDto> mapParams = new HashMap<>();
        ids.forEach(id -> {
            mapParams.putIfAbsent(id, new ParamEventDto(0L, 0L));
        });
        return mapParams;
    }

    private Event findEventById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found",
                        "The required object was not found."));
    }
}
