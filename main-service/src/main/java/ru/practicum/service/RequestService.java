package ru.practicum.service;

import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.Event;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto save(Long userId, Long eventId);

    ParticipationRequestDto update(Long userId, Long requestId);

    List<ParticipationRequestDto> findByUserId(Long userId);

    List<ParticipationRequestDto> findByEventId(Long eventId);

    EventRequestStatusUpdateResult updateRequestsStatus(EventRequestStatusUpdateRequest dto, Event event);

}
