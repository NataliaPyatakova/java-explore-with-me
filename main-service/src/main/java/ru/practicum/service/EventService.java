package ru.practicum.service;

import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventService {

    EventFullDto save(NewEventDto newEventDto, Long userId);

    EventFullDto updateByInitiator(UpdateEventUserRequest updateDto, Long eventId, Long userId);

    EventFullDto updateByAdmin(UpdateEventAdminRequest updateDto, Long eventId);

    EventFullDto findById(Long id);

    List<EventShortDto> findAllByIdIn(List<Long> ids);

    List<EventShortDto> findEventsByUserId(ParamEventFindByUser params);

    EventFullDto findEventByIdAndByUserId(Long eventId, Long userId);

    List<EventFullDto> findEventsByAdmin(ParamEventsFindByAdmin params);

    EventFullDto findPublishedEventById(Long id);

    List<EventShortDto> findPublishedEventsByParam(ParamEventsFindByParam params);

    List<ParticipationRequestDto> findRequestsByEventId(Long eventId, Long userId);

    EventRequestStatusUpdateResult updateRequestsStatus(EventRequestStatusUpdateRequest dto, Long eventId, Long userId);

    Map<Long, ParamEventDto> calcViewsAndRequests(List<Long> ids, LocalDateTime start);

}