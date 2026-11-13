package ru.practicum.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class CompilationMapperTest {

    private final NewCompilationDto newCompilationDto = new NewCompilationDto()
            .setTitle("test_title")
            .setPinned(true)
            .setEvents(null);
    private final Event eventWithModer = new Event()
            .setId(1L)
            .setTitle("test_title")
            .setRequestModeration(true);
    private final List<Event> events = new ArrayList<>();
    private final Compilation compilationWithNoEvents = new Compilation()
            .setId(1L)
            .setTitle("test_title")
            .setPinned(true)
            .setEvents(null);
    private final Compilation compilationWithEvents = new Compilation()
            .setId(2L)
            .setTitle("test_title1")
            .setPinned(true)
            .setEvents(events);
    private final Compilation foundedComp = new Compilation()
            .setId(3L)
            .setTitle("test_title3")
            .setPinned(true)
            .setEvents(events);
    private final List<Long> ids = new ArrayList<>();
    private final UpdateCompilationRequest dto = new UpdateCompilationRequest()
            .setTitle("updated_title")
            .setPinned(false)
            .setEvents(ids);
    private final List<EventShortDto> eventsShortDto = new ArrayList<>();
    private final EventShortDto eventShortDto = new EventShortDto()
            .setId(2L)
            .setTitle("eventShortDto_title")
            .setAnnotation("eventShortDto_annotation");

    @Test
    @DisplayName("Тест маппера из NewCompilationDto в Compilation без событий")
    void testMapToCompilationForCreateNoEvents() {
        Compilation result = CompilationMapper.mapToCompilationForCreate(newCompilationDto, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newCompilationDto.getTitle(), result.getTitle());
        Assertions.assertEquals(newCompilationDto.getPinned(), result.isPinned());
        Assertions.assertNull(result.getEvents());
    }

    @Test
    @DisplayName("Тест маппера из NewCompilationDto в Compilation с событиями")
    void testMapToCompilationForCreateWithEvents() {
        events.add(eventWithModer);
        Compilation result = CompilationMapper.mapToCompilationForCreate(newCompilationDto, events);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newCompilationDto.getTitle(), result.getTitle());
        Assertions.assertEquals(newCompilationDto.getPinned(), result.isPinned());
        Assertions.assertEquals(events, result.getEvents());
    }

    @Test
    @DisplayName("Тест маппера из Compilation в CompilationDto с событиями")
    void testMapToCompilationDtoFromCompilationWithEvents() {
        eventsShortDto.add(eventShortDto);
        CompilationDto result = CompilationMapper.mapToCompilationDtoFromCompilation(compilationWithEvents, eventsShortDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(compilationWithEvents.getId(), result.getId());
        Assertions.assertEquals(compilationWithEvents.getTitle(), result.getTitle());
        Assertions.assertEquals(compilationWithEvents.isPinned(), result.getPinned());
        Assertions.assertNotNull(result.getEvents());
        Assertions.assertEquals(eventShortDto.getId(), result.getEvents().getFirst().getId());
    }

    @Test
    @DisplayName("Тест маппера из Compilation в CompilationDto без событий")
    void testMapToCompilationDtoFromCompilationNoEvents() {
        CompilationDto result = CompilationMapper.mapToCompilationDtoFromCompilation(compilationWithNoEvents, eventsShortDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(compilationWithNoEvents.getId(), result.getId());
        Assertions.assertEquals(compilationWithNoEvents.getTitle(), result.getTitle());
        Assertions.assertEquals(compilationWithNoEvents.isPinned(), result.getPinned());
        Assertions.assertEquals(0, result.getEvents().size());
    }

    @Test
    @DisplayName("Тест маппера в Compilation для апдейта - все заполнено")
    void testMapToCompilationForUpdateAllFull() {
        ids.add(1L);
        events.add(eventWithModer);
        Compilation result = CompilationMapper.mapToCompilationForUpdate(foundedComp, dto, events);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedComp.getId(), result.getId());
        Assertions.assertEquals(dto.getTitle(), result.getTitle());
        Assertions.assertEquals(dto.getPinned(), result.isPinned());
        Assertions.assertNotNull(result.getEvents());
        Assertions.assertEquals(eventWithModer.getId(), result.getEvents().getFirst().getId());
    }

    @Test
    @DisplayName("Тест маппера в Compilation для апдейта - заполнено только title")
    void testMapToCompilationForUpdateTitle() {
        dto.setTitle("test_title");
        dto.setPinned(null);
        dto.setEvents(null);
        Compilation result = CompilationMapper.mapToCompilationForUpdate(foundedComp, dto, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedComp.getId(), result.getId());
        Assertions.assertEquals(dto.getTitle(), result.getTitle());
        Assertions.assertEquals(foundedComp.isPinned(), result.isPinned());
        Assertions.assertEquals(foundedComp.getEvents(), result.getEvents());
    }

    @Test
    @DisplayName("Тест маппера в Compilation для апдейта - заполнено только pinned")
    void testMapToCompilationForUpdatePinned() {
        dto.setTitle(null);
        dto.setPinned(false);
        dto.setEvents(null);
        Compilation result = CompilationMapper.mapToCompilationForUpdate(foundedComp, dto, null);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedComp.getId(), result.getId());
        Assertions.assertEquals(foundedComp.getTitle(), result.getTitle());
        Assertions.assertEquals(dto.getPinned(), result.isPinned());
        Assertions.assertEquals(foundedComp.getEvents(), result.getEvents());
    }

    @Test
    @DisplayName("Тест маппера в Compilation для апдейта - заполнено только events")
    void testMapToCompilationForUpdateEvents() {
        ids.add(1L);
        events.add(eventWithModer);
        dto.setTitle(null);
        dto.setPinned(null);
        dto.setEvents(ids);
        Compilation result = CompilationMapper.mapToCompilationForUpdate(foundedComp, dto, events);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedComp.getId(), result.getId());
        Assertions.assertEquals(foundedComp.getTitle(), result.getTitle());
        Assertions.assertEquals(foundedComp.isPinned(), result.isPinned());
        Assertions.assertEquals(events, result.getEvents());
    }

    @Test
    @DisplayName("Тест маппера в Compilation для апдейта - events не null но пустое")
    void testMapToCompilationForUpdateEventsEmpty() {
        Compilation result = CompilationMapper.mapToCompilationForUpdate(foundedComp, dto, events);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(foundedComp.getId(), result.getId());
        Assertions.assertEquals(dto.getTitle(), result.getTitle());
        Assertions.assertEquals(dto.getPinned(), result.isPinned());
        Assertions.assertNotNull(result.getEvents());
        Assertions.assertEquals(0, result.getEvents().size());
    }
}