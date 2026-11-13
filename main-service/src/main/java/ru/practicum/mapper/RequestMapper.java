package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enumeration.RequestStatus;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.model.User;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestMapper {

    public static Request mapToRequestForCreate(User user, Event event) {
        Request request = new Request();
        request.setCreated(LocalDateTime.now());
        request.setRequester(user);
        request.setEvent(event);
        //если для события отключена пре-модерация запросов на участие,
        // то запрос должен автоматически перейти в состояние подтвержденного
        //по тестам постамн - запросы на событие с нулевым лимитом сразу падают в CONFIRMED
        if (event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(event.isRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED);
        }
        return request;
    }

    public static ParticipationRequestDto mapToDtoFromRequest(Request request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setCreated(request.getCreated());
        dto.setStatus(request.getStatus());
        dto.setRequester(request.getRequester().getId());
        dto.setEvent(request.getEvent().getId());
        return dto;
    }
}