package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.EndpointHit;
import ru.practicum.dto.ViewStats;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
public class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StatsService statsService;

    private final LocalDateTime time = LocalDateTime.now();
    private final EndpointHit endpointHit = new EndpointHit()
            .setApp("testApp")
            .setUri("testUri")
            .setIp("testIp")
            .setTimestamp(time);
    private final ViewStats viewStats = new ViewStats()
            .setApp("testApp")
            .setUri("testUri")
            .setHits(1L);
    private final List<ViewStats> listViewStats = new ArrayList<>();
    List<String> uris = new ArrayList<>();

    @Test
    @DisplayName("Post hit - все верно")
    public void testSave() throws Exception {
        mockMvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHit))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Post hit - hit без app")
    public void testSaveWithoutApp() throws Exception {
        endpointHit.setApp(null);
        mockMvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHit))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post hit - hit без ip")
    public void testSaveWithoutIp() throws Exception {
        endpointHit.setIp(null);
        mockMvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHit))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post hit - hit без uri")
    public void testSaveWithoutUri() throws Exception {
        endpointHit.setUri(null);
        mockMvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHit))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post hit - hit без timestamp")
    public void testSaveWithoutTime() throws Exception {
        endpointHit.setTimestamp(null);
        mockMvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(endpointHit))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get stats - все хорошо - без необязательных параметров")
    public void testGetWithoutRequiredParam() throws Exception {
        listViewStats.add(viewStats);
        //необязательный параметр unique по дефолту встает в false
        when(statsService.getStats(time, time, null, false)).thenReturn(listViewStats);
        mockMvc.perform(get("/stats")
                        .param("start", String.valueOf(time))
                        .param("end", String.valueOf(time)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].app", is(viewStats.getApp())))
                .andExpect(jsonPath("$.[0].uri", is(viewStats.getUri())))
                .andExpect(jsonPath("$.[0].hits", is(viewStats.getHits()), Long.class));
    }

    @Test
    @DisplayName("Get stats - все хорошо - со всеми параметрами")
    public void testGetWithAllParams() throws Exception {
        listViewStats.add(viewStats);
        uris.add("testUri");
        when(statsService.getStats(time, time, uris, false)).thenReturn(listViewStats);
        mockMvc.perform(get("/stats")
                        .param("start", String.valueOf(time))
                        .param("end", String.valueOf(time))
                        .param("uris", "testUri")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].app", is(viewStats.getApp())))
                .andExpect(jsonPath("$.[0].uri", is(viewStats.getUri())))
                .andExpect(jsonPath("$.[0].hits", is(viewStats.getHits()), Long.class));
    }
}
