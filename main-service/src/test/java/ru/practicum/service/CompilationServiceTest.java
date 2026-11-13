package ru.practicum.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.ParamCompilationFindAll;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.enumeration.EventState;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CompilationServiceTest {

    @Autowired
    private CompilationService compilationService;
    private final EntityManager em;

    private final LocalDateTime date = LocalDateTime.now().plusDays(1);
    private final LocalDateTime now = LocalDateTime.now();
    private final Category category = new Category().setName("test_category");
    private final Location location = new Location().setLat(15.15).setLon(16.16);
    private final User initiator = new User()
            .setEmail("email_initiator@")
            .setName("initiator_name");
    private final List<Long> events = new ArrayList<>();
    private final List<Long> events2 = new ArrayList<>();
    private final NewCompilationDto newDto = new NewCompilationDto()
            .setTitle("New Compilation")
            .setPinned(true);
    private final NewCompilationDto newDto2 = new NewCompilationDto()
            .setTitle("New Compilation2")
            .setPinned(true);
    private final UpdateCompilationRequest updateDto = new UpdateCompilationRequest()
            .setTitle("Update Compilation")
            .setPinned(false);
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
    private final Event event2 = new Event()
            .setTitle("test_title2")
            .setDescription("test_description2")
            .setAnnotation("test_annotation2")
            .setEventDate(date)
            .setPaid(true)
            .setParticipantLimit(0)
            .setRequestModeration(true)
            .setCreatedOn(now)
            .setState(EventState.PUBLISHED);

    @Test
    @DisplayName("Сохранение compilation без событий- все хорошо")
    public void testSaveNoEvents() {
        CompilationDto result = compilationService.save(newDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newDto.getTitle(), result.getTitle());
        Assertions.assertEquals(newDto.getPinned(), result.getPinned());
        Assertions.assertEquals(0, result.getEvents().size());
    }

    @Test
    @DisplayName("Сохранение compilation с событием - все хорошо")
    public void testSaveWithEvents() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        events.add(event.getId());
        newDto.setEvents(events);
        CompilationDto result = compilationService.save(newDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newDto.getTitle(), result.getTitle());
        Assertions.assertEquals(newDto.getPinned(), result.getPinned());
        Assertions.assertEquals(1, result.getEvents().size());
        Assertions.assertEquals(event.getId(), result.getEvents().getFirst().getId());
    }

    @Test
    @DisplayName("Сохранение compilation с дублирующимся событием - все хорошо")
    public void testSaveWithEventsDuplicate() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        events.add(event.getId());
        newDto.setEvents(events);
        newDto.setEvents(events);
        CompilationDto result = compilationService.save(newDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newDto.getTitle(), result.getTitle());
        Assertions.assertEquals(newDto.getPinned(), result.getPinned());
        Assertions.assertEquals(1, result.getEvents().size());
        Assertions.assertEquals(event.getId(), result.getEvents().getFirst().getId());
    }

    @Test
    @DisplayName("Обновление compilation без события - новый title новый pinned")
    public void testUpdateNewTitleNewPinned() {
        CompilationDto compilationDto = compilationService.save(newDto);
        CompilationDto result = compilationService.update(compilationDto.getId(), updateDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(updateDto.getTitle(), result.getTitle());
        Assertions.assertEquals(updateDto.getPinned(), result.getPinned());
        Assertions.assertEquals(0, result.getEvents().size());
    }

    @Test
    @DisplayName("Обновление compilation - неверный compId")
    public void testUpdateWrongCompId() {
        try {
            compilationService.update(999L, updateDto);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Compilation with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление compilation без событий - добавляем события")
    public void testUpdateNoEventsWithNewEvents() {
        CompilationDto compilationDto = compilationService.save(newDto);
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        events.add(event.getId());
        updateDto.setEvents(events);
        CompilationDto result = compilationService.update(compilationDto.getId(), updateDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(updateDto.getTitle(), result.getTitle());
        Assertions.assertEquals(updateDto.getPinned(), result.getPinned());
        Assertions.assertEquals(1, result.getEvents().size());
        Assertions.assertEquals(event.getId(), result.getEvents().getFirst().getId());
    }

    @Test
    @DisplayName("Обновление compilation c событиями - меняем события")
    public void testUpdateWithEventsWithNewEvents() {
        //старое событие
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        events.add(event.getId());
        newDto.setEvents(events);
        CompilationDto compilationDto = compilationService.save(newDto);
        //новое событие
        event2.setInitiator(initiator);
        event2.setCategory(category);
        event2.setLocation(location);
        em.persist(event2);
        events2.add(event2.getId());
        updateDto.setEvents(events2);
        CompilationDto result = compilationService.update(compilationDto.getId(), updateDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(updateDto.getTitle(), result.getTitle());
        Assertions.assertEquals(updateDto.getPinned(), result.getPinned());
        Assertions.assertEquals(1, result.getEvents().size());
        Assertions.assertEquals(event2.getId(), result.getEvents().getFirst().getId());
    }

    @Test
    @DisplayName("Удаление compilation - все хорошо")
    void testDelete() {
        CompilationDto result = compilationService.save(newDto);
        compilationService.delete(result.getId());
    }

    @Test
    @DisplayName("Удаление compilation - неверный compId")
    void testDeleteWrongCompId() {
        try {
            compilationService.delete(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Compilation with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск compilation без событий - все хорошо")
    void testFindById() {
        CompilationDto compilationDto = compilationService.save(newDto);
        CompilationDto result = compilationService.findById(compilationDto.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newDto.getTitle(), result.getTitle());
        Assertions.assertEquals(newDto.getPinned(), result.getPinned());
        Assertions.assertEquals(0, result.getEvents().size());
    }

    @Test
    @DisplayName("Поиск compilation с событиями - все хорошо")
    void testFindByIdWithEvents() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        events.add(event.getId());
        newDto.setEvents(events);
        CompilationDto compilationDto = compilationService.save(newDto);
        CompilationDto result = compilationService.findById(compilationDto.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newDto.getTitle(), result.getTitle());
        Assertions.assertEquals(newDto.getPinned(), result.getPinned());
        Assertions.assertEquals(1, result.getEvents().size());
        Assertions.assertEquals(event.getId(), result.getEvents().getFirst().getId());
    }

    @Test
    @DisplayName("Поиск compilation по id - неверный compId")
    void testFindByIdWrongCompId() {
        try {
            compilationService.findById(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Compilation with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск compilation all - пустой список")
    void testFindAllEmptyList() {
        ParamCompilationFindAll params = new ParamCompilationFindAll(true, 0, 10);
        List<CompilationDto> result = compilationService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск compilation all - pinned = true from = 0 , size = 10")
    void testFindAllPinnedTrueFrom0Size10() {
        CompilationDto compilationDto = compilationService.save(newDto);
        ParamCompilationFindAll params = new ParamCompilationFindAll(true, 0, 10);
        List<CompilationDto> result = compilationService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(compilationDto.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("Поиск compilation all - pinned = false from = 0 , size = 10")
    void testFindAllPinnedFalseFrom0Size10() {
        newDto.setPinned(false);
        CompilationDto compilationDto = compilationService.save(newDto);
        ParamCompilationFindAll params = new ParamCompilationFindAll(false, 0, 10);
        List<CompilationDto> result = compilationService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(compilationDto.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("Поиск compilation all - pinned = null from = 0 , size = 10")
    void testFindAllPinnedNullFrom0Size10() {
        CompilationDto compilationDto = compilationService.save(newDto);
        ParamCompilationFindAll params = new ParamCompilationFindAll(null, 0, 10);
        List<CompilationDto> result = compilationService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(compilationDto.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("Поиск compilation all - pinned = null from = 0 , size = 1")
    void testFindAllPinnedNullFrom0Size1() {
        CompilationDto compilationDto = compilationService.save(newDto);
        compilationService.save(newDto2);
        ParamCompilationFindAll params = new ParamCompilationFindAll(null, 0, 1);
        List<CompilationDto> result = compilationService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(compilationDto.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("Поиск compilation all - pinned = null from = 0 , size = 2")
    void testFindAllPinnedNullFrom0Size2() {
        CompilationDto compilationDto = compilationService.save(newDto);
        CompilationDto compilationDto2 = compilationService.save(newDto2);
        ParamCompilationFindAll params = new ParamCompilationFindAll(null, 0, 2);
        List<CompilationDto> result = compilationService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(compilationDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(compilationDto2.getId(), result.getLast().getId());
    }

    @Test
    @DisplayName("Поиск compilation all с событиями - pinned = null from = 0 , size = 10")
    void testFindAllWithEventsPinnedNullFrom0Size2() {
        em.persist(initiator);
        em.persist(category);
        em.persist(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setLocation(location);
        em.persist(event);
        events.add(event.getId());
        newDto.setEvents(events);
        newDto2.setEvents(events);
        CompilationDto compilationDto = compilationService.save(newDto);
        CompilationDto compilationDto2 = compilationService.save(newDto2);
        ParamCompilationFindAll params = new ParamCompilationFindAll(null, 0, 10);
        List<CompilationDto> result = compilationService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals(compilationDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(event.getId(), result.getFirst().getEvents().getFirst().getId());
        Assertions.assertEquals(compilationDto2.getId(), result.getLast().getId());
        Assertions.assertEquals(event.getId(), result.getLast().getEvents().getFirst().getId());
    }
}