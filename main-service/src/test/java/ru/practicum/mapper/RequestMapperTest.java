package ru.practicum.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enumeration.RequestStatus;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.model.User;

import java.time.LocalDateTime;

@SpringBootTest
public class RequestMapperTest {

    private final LocalDateTime date = LocalDateTime.now();
    private final User requester = new User()
            .setId(1L)
            .setEmail("test_email")
            .setName("test_name");
    private final Event eventWithModer = new Event()
            .setId(1L)
            .setTitle("test_title")
            .setRequestModeration(true);
    private final Event eventNoModer = new Event()
            .setId(2L)
            .setTitle("test_title2")
            .setRequestModeration(false);
    private final Request request = new Request()
            .setId(1L)
            .setRequester(requester)
            .setEvent(eventWithModer)
            .setCreated(date)
            .setStatus(RequestStatus.PENDING);

    @Test
    @DisplayName("Тест маппера из сущностей в Request с модерацией")
    void testFromEntityToRequestWithModer() {
        Request result = RequestMapper.mapToRequestForCreate(requester, eventWithModer);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(RequestStatus.PENDING, result.getStatus());
        Assertions.assertEquals(requester, result.getRequester());
        Assertions.assertEquals(eventWithModer, result.getEvent());
    }

    @Test
    @DisplayName("Тест маппера из сущностей в Request без модерации")
    void testFromEntityToRequestNoModer() {
        Request result = RequestMapper.mapToRequestForCreate(requester, eventNoModer);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(RequestStatus.CONFIRMED, result.getStatus());
        Assertions.assertEquals(requester, result.getRequester());
        Assertions.assertEquals(eventNoModer, result.getEvent());
    }

    @Test
    @DisplayName("Тест маппера из Request в ParticipationRequestDto")
    void testFromRequestToParticipationRequestDto() {
        ParticipationRequestDto result = RequestMapper.mapToDtoFromRequest(request);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(request.getId(), result.getId());
        Assertions.assertEquals(request.getCreated(), result.getCreated());
        Assertions.assertEquals(request.getStatus(), result.getStatus());
        Assertions.assertEquals(request.getRequester().getId(), result.getRequester());
        Assertions.assertEquals(request.getEvent().getId(), result.getEvent());
    }
}
