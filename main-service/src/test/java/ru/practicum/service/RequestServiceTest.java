package ru.practicum.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enumeration.EventState;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;

import java.time.LocalDateTime;

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

//TODO ждем сервиса событий
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
        //  Assertions.assertEquals(newCategoryDto.getName(), result.getName());
    }
}
