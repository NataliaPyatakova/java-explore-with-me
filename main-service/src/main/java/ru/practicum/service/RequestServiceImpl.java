package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventCountRequests;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enumeration.EventState;
import ru.practicum.enumeration.RequestStatus;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.RequestRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final UserService userService;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public ParticipationRequestDto save(Long userId, Long eventId) {
        log.info("save request userId {} eventId {}", userId, eventId);
        User user = UserMapper.mapToUserFromUserDto(userService.findById(userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found",
                        "The required object was not found."));
        //инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)
        if (userId.equals(event.getInitiator().getId())) {
            throw new ValidationException("Validation Error",
                    "Initiator of event could not make request for participation");
        }
        //нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ValidationException("Validation Error",
                    "Event wasn't published");
        }
        //если у события указан лимит - проверим-ка сколько у него одобренных заявок
        if (event.getParticipantLimit() > 0) {
            List<Request> request = requestRepository.findByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            //если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
            if (!request.isEmpty() && request.size() >= event.getParticipantLimit()) {
                throw new ValidationException("The participant limit has been reached",
                        "For the requested operation the conditions are not met.");
            }
        }
        Request request = requestRepository.save(RequestMapper.mapToRequestForCreate(user, event));
        return RequestMapper.mapToDtoFromRequest(request);
    }

    @Override
    @Transactional
    public ParticipationRequestDto update(Long userId, Long requestId) {
        log.info("update request userId {} requestId {}", userId, requestId);
        userService.findById(userId);
        Request request = findRequestById(requestId);
        //Пока только отмена - без редактирования
        request.setStatus(RequestStatus.CANCELED);
        return RequestMapper.mapToDtoFromRequest(request);
    }

    @Override
    public List<ParticipationRequestDto> findByUserId(Long userId) {
        log.info("findByUserId request userId {}", userId);
        userService.findById(userId);
        List<Request> requests = requestRepository.findByRequesterId(userId);
        return requests.stream().map(RequestMapper::mapToDtoFromRequest).toList();
    }

    @Override
    public List<ParticipationRequestDto> findByEventId(Long eventId) {
        log.info("findByEventId request eventId {}", eventId);
        eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found",
                        "The required object was not found."));
        List<Request> requests = requestRepository.findByEventId(eventId);
        return requests.stream().map(RequestMapper::mapToDtoFromRequest).toList();
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(EventRequestStatusUpdateRequest dto, Event event) {
        log.info("updateRequestsStatus EventRequestStatusUpdateRequest {}", dto);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        //если у события указан лимит - проверим-ка сколько у него одобренных заявок
        //если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить
        //только для ПОДТВЕРЖДЕНИЯ
        if (dto.getStatus().equals(RequestStatus.CONFIRMED) && event.getParticipantLimit() > 0) {
            List<Request> request = requestRepository.findByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
            //если у события достигнут лимит запросов на участие - необходимо вернуть ошибку (Ожидается код ошибки 409)
            if (!request.isEmpty() //заявки были
                    && (request.size() >= event.getParticipantLimit() //и их уже больше или равно лимиту
                    || request.size() + dto.getRequestIds().size() > event.getParticipantLimit())) { //или же они плюс то что на входе превысят лимит
                throw new ValidationException("The participant limit has been reached",
                        "For the requested operation the conditions are not met.");
            }
            //заявок подтвержденных не было, но сам размер заявок на входе больше лимита
            if (request.isEmpty() && dto.getRequestIds().size() > event.getParticipantLimit()) {
                throw new ValidationException("The participant limit has been reached",
                        "For the requested operation the conditions are not met.");
            }
        }
        List<Request> requests = requestRepository.findAllById(dto.getRequestIds());
        //сначала проверяем корректность данных - если хоть одтн косяк на выход
        requests.forEach(request -> {
            if (!request.getEvent().getId().equals(event.getId())) {
                throw new BadRequestException("Requests must belong to current Event",
                        "Incorrectly made request.");
            }
            //заявки должны быть все в состоянии PENDING 409
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new BadRequestException("Request must have status PENDING",
                        "Incorrectly made request.");
            }
        });
        //теперь передергиваем данные
        requests.forEach(request -> {
            if (dto.getStatus().equals(RequestStatus.CONFIRMED)) {
                request.setStatus(RequestStatus.CONFIRMED);
                result.getConfirmedRequests().add(RequestMapper.mapToDtoFromRequest(request));
            }
            if (dto.getStatus().equals(RequestStatus.REJECTED)) {
                request.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(RequestMapper.mapToDtoFromRequest(request));
            }
        });
        return result;
    }

    @Override
    public List<EventCountRequests> countRequestsByEventId(List<Long> ids, RequestStatus status) {
        return requestRepository.countRequestsByEventId(ids, status);
    }

    private Request findRequestById(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Request with id=" + id + " was not found",
                        "The required object was not found."));
    }
}