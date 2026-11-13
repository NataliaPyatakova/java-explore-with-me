package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.event.*;
import ru.practicum.enumeration.AdminEventStateAction;
import ru.practicum.enumeration.EventState;
import ru.practicum.enumeration.UserEventStateAction;
import ru.practicum.model.Event;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EventMapper {

    //для save EventService
    public static Event mapFromNewEventDtoToEvent(NewEventDto newDto, ParamEventSave paramEventSave) {
        Event event = new Event();
        event.setTitle(newDto.getTitle());
        event.setDescription(newDto.getDescription());
        event.setAnnotation(newDto.getAnnotation());
        event.setEventDate(newDto.getEventDate());
        event.setCategory(paramEventSave.getCategory());
        event.setLocation(paramEventSave.getLocation());
        event.setPaid(newDto.getPaid());
        event.setParticipantLimit(newDto.getParticipantLimit());
        event.setRequestModeration(newDto.getRequestModeration());
        event.setCreatedOn(LocalDateTime.now());
        event.setInitiator(paramEventSave.getUser());
        event.setState(EventState.PENDING);
        return event;
    }

    //для EventService findAllByIdIn
    //для CompilationMapper
    public static EventShortDto mapFromEventToEventShortDto(Event event, ParamEventDto params) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setId(event.getId());
        eventShortDto.setTitle(event.getTitle());
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setCategory(CategoryMapper.mapToCategoryDto(event.getCategory()));
        eventShortDto.setConfirmedRequests(params.getConfirmedRequests());
        eventShortDto.setInitiator(UserMapper.mapToUserShortDto(event.getInitiator()));
        eventShortDto.setPaid(event.isPaid());
        eventShortDto.setViews(params.getViews());
        return eventShortDto;
    }

    //для EventServiceImpl save findByID
    public static EventFullDto mapFromEventToEventFullDto(Event event, ParamEventDto params) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(event.getId());
        eventFullDto.setTitle(event.getTitle());
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setEventDate(event.getEventDate());
        eventFullDto.setCategory(CategoryMapper.mapToCategoryDto(event.getCategory()));
        eventFullDto.setConfirmedRequests(params.getConfirmedRequests());
        eventFullDto.setCreatedOn(event.getCreatedOn());
        eventFullDto.setInitiator(UserMapper.mapToUserShortDto(event.getInitiator()));
        eventFullDto.setLocation(LocationMapper.mapToLocationDtoFromLocation(event.getLocation()));
        eventFullDto.setPaid(event.isPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());
        eventFullDto.setPublishedOn(event.getPublishedOn());
        eventFullDto.setRequestModeration(event.isRequestModeration());
        eventFullDto.setState(event.getState());
        eventFullDto.setViews(params.getViews());
        return eventFullDto;
    }

    //для CompilationServiceImpl
    //хватает урезанного варианта
    public static Event mapFromEventShortDtoToEvent(EventShortDto dto) {
        Event event = new Event();
        event.setId(dto.getId());
        event.setTitle(dto.getTitle());
        event.setAnnotation(dto.getAnnotation());
        event.setEventDate(dto.getEventDate());
        event.setCategory(CategoryMapper.mapToCategoryFromCategoryDto(dto.getCategory()));
        event.setPaid(dto.getPaid());
        event.setInitiator(UserMapper.mapToUserFromUserShortDto(dto.getInitiator()));
        return event;
    }

    //для RequestServiceImpl
    //обязательно нужны Initiator State ParticipantLimit
    public static Event mapFromEventFullDtoToEvent(EventFullDto dto) {
        Event event = new Event();
        event.setId(dto.getId());
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setAnnotation(dto.getAnnotation());
        event.setEventDate(dto.getEventDate());
        event.setCategory(CategoryMapper.mapToCategoryFromCategoryDto(dto.getCategory()));
        event.setLocation(LocationMapper.mapToLocationFromLocationDto(dto.getLocation()));
        event.setPaid(dto.getPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
        event.setCreatedOn(dto.getCreatedOn());
        event.setInitiator(UserMapper.mapToUserFromUserShortDto(dto.getInitiator()));
        event.setPublishedOn(dto.getPublishedOn());
        event.setState(dto.getState());
        return event;
    }

    //для EventService updateByInitiator
    public static Event mapFromUpdateEventUserRequestToEvent(UpdateEventUserRequest dto, ParamEventUpdate params) {
        Event event = mapToEvent(dto, params);
        if (UserEventStateAction.CANCEL_REVIEW.equals(dto.getStateAction())) {
            event.setState(EventState.CANCELED);
        }
        if (UserEventStateAction.SEND_TO_REVIEW.equals(dto.getStateAction())) {
            event.setState(EventState.PENDING);
        }
        if (dto.getStateAction() == null) {
            event.setState(params.getEvent().getState());
        }
        return event;
    }

    //для EventService updateByAdmin
    public static Event mapFromUpdateEventAdminRequestToEvent(UpdateEventAdminRequest dto, ParamEventUpdate params) {
        Event event = mapToEvent(dto, params);
        if (AdminEventStateAction.PUBLISH_EVENT.equals(dto.getStateAction())) {
            event.setPublishedOn(LocalDateTime.now());
            event.setState(EventState.PUBLISHED);
        }
        if (AdminEventStateAction.REJECT_EVENT.equals(dto.getStateAction())) {
            event.setState(EventState.CANCELED);
        }
        if (dto.getStateAction() == null) {
            event.setState(params.getEvent().getState());
        }
        return event;
    }

    private static <T extends UpdateEventRequest> Event mapToEvent(T dto, ParamEventUpdate params) {
        //Если поле в запросе не указано (равно null) - значит изменение этих данных не треубется.
        Event event = new Event();
        event.setId(params.getEvent().getId());
        //эти поля просто перекидываем чтобы не затерлись при апдейте
        event.setInitiator(params.getEvent().getInitiator());
        event.setCreatedOn(params.getEvent().getCreatedOn());
        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        } else {
            event.setTitle(params.getEvent().getTitle());
        }
        if (dto.getAnnotation() != null) {
            event.setAnnotation(dto.getAnnotation());
        } else {
            event.setAnnotation(params.getEvent().getAnnotation());
        }
        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        } else {
            event.setDescription(params.getEvent().getDescription());
        }
        if (dto.getEventDate() != null) {
            event.setEventDate(dto.getEventDate());
        } else {
            event.setEventDate(params.getEvent().getEventDate());
        }
        if (params.getCategory() != null) {
            event.setCategory(params.getCategory());
        } else {
            event.setCategory(params.getEvent().getCategory());
        }
        if (params.getLocation() != null) {
            event.setLocation(params.getLocation());
        } else {
            event.setLocation(params.getEvent().getLocation());
        }
        if (dto.getPaid() != null) {
            event.setPaid(dto.getPaid());
        } else {
            event.setPaid(params.getEvent().isPaid());
        }
        if (dto.getParticipantLimit() != null) {
            event.setParticipantLimit(dto.getParticipantLimit());
        } else {
            event.setParticipantLimit(params.getEvent().getParticipantLimit());
        }
        if (dto.getRequestModeration() != null) {
            event.setRequestModeration(dto.getRequestModeration());
        } else {
            event.setRequestModeration(params.getEvent().isRequestModeration());
        }
        return event;
    }
}