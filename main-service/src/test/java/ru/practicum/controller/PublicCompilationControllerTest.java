package ru.practicum.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.compilation.ParamCompilationFindAll;
import ru.practicum.service.CompilationService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCompilationController.class)
public class PublicCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompilationService compilationService;

    private final List<EventShortDto> eventsShortList = new ArrayList<>();
    private final EventShortDto eventShort = new EventShortDto().setId(1L);
    private final List<CompilationDto> compilationDtoList = new ArrayList<>();
    private final CompilationDto compilationDto = new CompilationDto()
            .setId(1L)
            .setTitle("test")
            .setPinned(false)
            .setEvents(eventsShortList);

    @Test
    @DisplayName("Get compilation без событий - все хорошо")
    void testFindByIdAllOk() throws Exception {
        when(compilationService.findById(compilationDto.getId())).thenReturn(compilationDto);
        mockMvc.perform(get("/compilations/" + compilationDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(compilationDto.getPinned())));
    }

    @Test
    @DisplayName("Get compilation с событиями - все хорошо")
    void testFindByIdWithEventsAllOk() throws Exception {
        eventsShortList.add(eventShort);
        when(compilationService.findById(compilationDto.getId())).thenReturn(compilationDto);
        mockMvc.perform(get("/compilations/" + compilationDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.events.[0].id", is(eventShort.getId()), Long.class));
    }

    @Test
    @DisplayName("Get compilation - некорректный параметр")
    void testFindByIdError400() throws Exception {
        mockMvc.perform(get("/compilations/a"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get compilation - null параметр")
    void testFindByIdError500() throws Exception {
        mockMvc.perform(get("/compilations/"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Get all compilation - все параметры - все хорошо")
    void testFindAllOk() throws Exception {
        eventsShortList.add(eventShort);
        compilationDtoList.add(compilationDto);
        ParamCompilationFindAll params = new ParamCompilationFindAll(false, 0, 10);
        when(compilationService.findAll(params)).thenReturn(compilationDtoList);
        mockMvc.perform(get("/compilations")
                        .param("pinned", "false")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.[0].pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.[0].events.[0].id", is(eventShort.getId()), Long.class));
    }

    @Test
    @DisplayName("Get all compilation - без pinned - все хорошо")
    void testFindAllWithOutPinnedOk() throws Exception {
        eventsShortList.add(eventShort);
        compilationDtoList.add(compilationDto);
        ParamCompilationFindAll params = new ParamCompilationFindAll(null, 0, 10);
        when(compilationService.findAll(params)).thenReturn(compilationDtoList);
        mockMvc.perform(get("/compilations")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.[0].pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.[0].events.[0].id", is(eventShort.getId()), Long.class));
    }

    @Test
    @DisplayName("Get all compilation - без from - все хорошо")
    void testFindAllWithOutFromOk() throws Exception {
        eventsShortList.add(eventShort);
        compilationDtoList.add(compilationDto);
        ParamCompilationFindAll params = new ParamCompilationFindAll(false, 0, 10);
        when(compilationService.findAll(params)).thenReturn(compilationDtoList);
        mockMvc.perform(get("/compilations")
                        .param("pinned", "false")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.[0].pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.[0].events.[0].id", is(eventShort.getId()), Long.class));
    }

    @Test
    @DisplayName("Get all compilation - без size - все хорошо")
    void testFindAllWithOutSizeOk() throws Exception {
        eventsShortList.add(eventShort);
        compilationDtoList.add(compilationDto);
        ParamCompilationFindAll params = new ParamCompilationFindAll(false, 0, 10);
        when(compilationService.findAll(params)).thenReturn(compilationDtoList);
        mockMvc.perform(get("/compilations")
                        .param("pinned", "false")
                        .param("from", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.[0].pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.[0].events.[0].id", is(eventShort.getId()), Long.class));
    }
}