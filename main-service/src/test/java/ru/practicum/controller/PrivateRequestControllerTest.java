package ru.practicum.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.enumeration.RequestStatus;
import ru.practicum.service.RequestService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PrivateRequestController.class)
public class PrivateRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RequestService requestService;

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LocalDateTime now = LocalDateTime.now();
    private final ParticipationRequestDto participationRequestDto = new ParticipationRequestDto()
            .setId(1L)
            .setRequester(1L)
            .setEvent(1L)
            .setStatus(RequestStatus.PENDING)
            .setCreated(now);
    private final ParticipationRequestDto canceledParticipationRequestDto = new ParticipationRequestDto()
            .setId(1L)
            .setRequester(1L)
            .setEvent(1L)
            .setStatus(RequestStatus.CANCELED)
            .setCreated(now);
    List<ParticipationRequestDto> list = new ArrayList<>();

    @Test
    @DisplayName("Post request - все хорошо")
    void testSaveOk() throws Exception {
        when(requestService.save(participationRequestDto.getRequester(), participationRequestDto.getEvent()))
                .thenReturn(participationRequestDto);
        mockMvc.perform(post("/private/users/1/requests/1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(participationRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.event", is(participationRequestDto.getEvent()), Long.class))
                .andExpect(jsonPath("$.requester", is(participationRequestDto.getRequester()), Long.class))
                .andExpect(jsonPath("$.created", is(participationRequestDto.getCreated().format(dtf))))
                .andExpect(jsonPath("$.status", is(participationRequestDto.getStatus().toString())));
    }

    @Test
    @DisplayName("Patch request - все хорошо")
    void testUpdateOk() throws Exception {
        when(requestService.update(participationRequestDto.getRequester(), participationRequestDto.getId()))
                .thenReturn(canceledParticipationRequestDto);
        mockMvc.perform(patch("/private/users/1/requests/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(canceledParticipationRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.event", is(canceledParticipationRequestDto.getEvent()), Long.class))
                .andExpect(jsonPath("$.requester", is(canceledParticipationRequestDto.getRequester()), Long.class))
                .andExpect(jsonPath("$.created", is(canceledParticipationRequestDto.getCreated().format(dtf))))
                .andExpect(jsonPath("$.status", is(canceledParticipationRequestDto.getStatus().toString())));
    }

    @Test
    @DisplayName("Get request - все хорошо")
    void testFindByUserIdAllOk() throws Exception {
        list.add(participationRequestDto);
        when(requestService.findByUserId(participationRequestDto.getRequester())).thenReturn(list);
        mockMvc.perform(get("/private/users/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(participationRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].event", is(participationRequestDto.getEvent()), Long.class))
                .andExpect(jsonPath("$.[0].requester", is(participationRequestDto.getRequester()), Long.class))
                .andExpect(jsonPath("$.[0].created", is(participationRequestDto.getCreated().format(dtf))))
                .andExpect(jsonPath("$.[0].status", is(participationRequestDto.getStatus().toString())));
    }

    @Test
    @DisplayName("Get request - некорректный параметр")
    void testFindByUserIdError400() throws Exception {
        mockMvc.perform(get("/private/users/a/requests"))
                .andExpect(status().isBadRequest());
    }
}