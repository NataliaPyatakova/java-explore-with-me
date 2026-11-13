package ru.practicum.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@RequiredArgsConstructor
@Validated
public class PrivateRequestController {

    private final RequestService requestService;

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto save(@PathVariable @NotNull Long userId,
                                        @RequestParam(value = "eventId") @NotNull Long eventId) {
        log.info("PrivateRequestController save request userId {} eventId {}", userId, eventId);
        return requestService.save(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto update(@PathVariable @NotNull Long userId,
                                          @PathVariable @NotNull Long requestId) {
        log.info("PrivateRequestController update userId {} requestId {}", userId, requestId);
        return requestService.update(userId, requestId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> findByUserId(@PathVariable @NotNull Long userId) {
        log.info("PrivateRequestController findByUserId userId {}", userId);
        return requestService.findByUserId(userId);
    }
}