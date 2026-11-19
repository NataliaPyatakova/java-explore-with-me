package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.CompilationService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCompilationController.class)
public class AdminCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CompilationService compilationService;

    private final List<Long> events = new ArrayList<>();
    private final List<EventShortDto> eventsShortList = new ArrayList<>();
    private final EventShortDto eventShort = new EventShortDto().setId(1L);
    private final NewCompilationDto newDto = new NewCompilationDto()
            .setTitle("title")
            .setPinned(true)
            .setEvents(events);
    private final CompilationDto compilationDto = new CompilationDto()
            .setId(1L)
            .setTitle("title")
            .setPinned(true)
            .setEvents(eventsShortList);
    private final UpdateCompilationRequest updateDto = new UpdateCompilationRequest()
            .setTitle("title_updated")
            .setPinned(true)
            .setEvents(events);
    private final CompilationDto updatedCompilationDto = new CompilationDto()
            .setId(1L)
            .setTitle("title_updated")
            .setPinned(true)
            .setEvents(eventsShortList);

    @Test
    @DisplayName("Post compilation без событий - все хорошо")
    void testSaveOk() throws Exception {
        newDto.setEvents(null);
        when(compilationService.save(newDto)).thenReturn(compilationDto);
        mockMvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(compilationDto.getPinned())));
    }

    @Test
    @DisplayName("Post compilation с событиями - все хорошо")
    void testSaveWithEventsOk() throws Exception {
        events.add(eventShort.getId());
        eventsShortList.add(eventShort);
        when(compilationService.save(newDto)).thenReturn(compilationDto);
        mockMvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(compilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(compilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(compilationDto.getPinned())))
                .andExpect(jsonPath("$.events.[0].id", is(eventShort.getId()), Long.class));
    }

    @Test
    @DisplayName("Post compilation - title null")
    void testSaveNullTitle() throws Exception {
        newDto.setTitle(null);
        mockMvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post compilation - title пустое")
    void testSaveEmptyTitle() throws Exception {
        newDto.setTitle("");
        mockMvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post compilation - title из пробелов")
    void testSaveBlankTitle() throws Exception {
        newDto.setTitle("    ");
        mockMvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post compilation - title больше 50")
    void testSaveTitle50() throws Exception {
        newDto.setTitle("ф".repeat(51));
        mockMvc.perform(post("/admin/compilations")
                        .content(mapper.writeValueAsString(newDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Delete compilation по id")
    public void testDelete() throws Exception {
        mockMvc.perform(delete("/admin/compilations/" + compilationDto.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Patch compilation - все хорошо")
    void testPatchOk() throws Exception {
        events.add(eventShort.getId());
        eventsShortList.add(eventShort);
        when(compilationService.update(1L, updateDto)).thenReturn(updatedCompilationDto);
        mockMvc.perform(patch("/admin/compilations/1")
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedCompilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(updatedCompilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(updatedCompilationDto.getPinned())))
                .andExpect(jsonPath("$.events.[0].id", is(eventShort.getId()), Long.class));
    }

    @Test
    @DisplayName("Patch compilation - поле title не задано = null - все хорошо")
    void testPatchTitleNullOk() throws Exception {
        updateDto.setTitle(null);
        events.add(eventShort.getId());
        eventsShortList.add(eventShort);
        when(compilationService.update(1L, updateDto)).thenReturn(updatedCompilationDto);
        mockMvc.perform(patch("/admin/compilations/1")
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedCompilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(updatedCompilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(updatedCompilationDto.getPinned())))
                .andExpect(jsonPath("$.events.[0].id", is(eventShort.getId()), Long.class));
    }

    @Test
    @DisplayName("Patch compilation - title пустое")
    void testPatchEmptyName() throws Exception {
        updateDto.setTitle("");
        mockMvc.perform(patch("/admin/compilations/1")
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch compilation - title больше 50")
    void testPatchName50() throws Exception {
        updateDto.setTitle("ф".repeat(51));
        mockMvc.perform(patch("/admin/compilations/1")
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch compilation - поле pinned не задано = null - все хорошо")
    void testPatchPinnedNullOk() throws Exception {
        updateDto.setPinned(null);
        events.add(eventShort.getId());
        eventsShortList.add(eventShort);
        when(compilationService.update(1L, updateDto)).thenReturn(updatedCompilationDto);
        mockMvc.perform(patch("/admin/compilations/1")
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedCompilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(updatedCompilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(updatedCompilationDto.getPinned())))
                .andExpect(jsonPath("$.events.[0].id", is(eventShort.getId()), Long.class));
    }

    @Test
    @DisplayName("Patch compilation - поле events не задано = null - все хорошо")
    void testPatchEventsNullOk() throws Exception {
        updateDto.setEvents(null);
        eventsShortList.add(eventShort);
        when(compilationService.update(1L, updateDto)).thenReturn(updatedCompilationDto);
        mockMvc.perform(patch("/admin/compilations/1")
                        .content(mapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedCompilationDto.getId()), Long.class))
                .andExpect(jsonPath("$.title", is(updatedCompilationDto.getTitle())))
                .andExpect(jsonPath("$.pinned", is(updatedCompilationDto.getPinned())))
                .andExpect(jsonPath("$.events.[0].id", is(eventShort.getId()), Long.class));
    }
}