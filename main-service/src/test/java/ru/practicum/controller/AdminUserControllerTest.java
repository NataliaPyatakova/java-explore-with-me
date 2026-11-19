package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.ParamUserFindAll;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminUserController.class)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    private final NewUserRequest newUserRequest = new NewUserRequest()
            .setEmail("test_email@email.ru")
            .setName("test_name");
    private final UserDto userDto = new UserDto()
            .setId(1L)
            .setEmail("test_email@email.ru")
            .setName("test_name");
    private final List<Long> ids = new ArrayList<>();

    @Test
    @DisplayName("Post user - все хорошо")
    void testSaveOk() throws Exception {
        when(userService.save(newUserRequest)).thenReturn(userDto);
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.email", is(userDto.getEmail()), String.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())));
    }

    @Test
    @DisplayName("Post user - null email")
    void testSaveNullEmail() throws Exception {
        newUserRequest.setEmail(null);
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - null name")
    void testSaveNullName() throws Exception {
        newUserRequest.setName(null);
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - empty email")
    void testSaveEmptyEmail() throws Exception {
        newUserRequest.setEmail("");
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - empty name")
    void testSaveEmptyName() throws Exception {
        newUserRequest.setName("");
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - blank email")
    void testSaveBlankEmail() throws Exception {
        newUserRequest.setEmail("   ");
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - blank name")
    void testSaveBlankName() throws Exception {
        newUserRequest.setName("   ");
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - name длиной 1 символ")
    void testSaveName1symbol() throws Exception {
        newUserRequest.setName("s");
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - name длиной 260 символов")
    void testSaveName260symbol() throws Exception {
        newUserRequest.setName("a".repeat(260));
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - email длиной 1 символ")
    void testSaveEmail1symbol() throws Exception {
        newUserRequest.setEmail("a".repeat(1));
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - email длиной 260 символов")
    void testSaveEmail260symbol() throws Exception {
        newUserRequest.setEmail("a".repeat(260));
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - email не в формате")
    void testSaveEmailNotFormatted() throws Exception {
        newUserRequest.setEmail("test_email");
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - email local part больше 64")
    void testSaveEmailLocalPart65() throws Exception {
        String email = "a".repeat(65) + "@test.ru";
        newUserRequest.setEmail(email);
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post user - email domain part больше 64")
    void testSaveEmailDomainPart65() throws Exception {
        String email = "test_email@" + "a".repeat(65) + ".ru";
        newUserRequest.setEmail(email);
        mockMvc.perform(post("/admin/users")
                        .content(mapper.writeValueAsString(newUserRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get user - без списка - все хорошо")
    void testGetAllWithOutIdsOk() throws Exception {
        ParamUserFindAll paramUserFindAll = new ParamUserFindAll(null, 0, 10);
        List<UserDto> userDtos = new ArrayList<>();
        userDtos.add(userDto);
        when(userService.findAll(paramUserFindAll)).thenReturn(userDtos);
        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$.[0].email", is(userDto.getEmail()), String.class));
    }

    @Test
    @DisplayName("Get user - со списком - все хорошо")
    void testGetAllWithIdsOk() throws Exception {
        ids.add(userDto.getId());
        ParamUserFindAll paramUserFindAll = new ParamUserFindAll(ids, 0, 10);
        List<UserDto> userDtos = new ArrayList<>();
        userDtos.add(userDto);
        when(userService.findAll(paramUserFindAll)).thenReturn(userDtos);
        mockMvc.perform(get("/admin/users")
                        .param("ids", "1")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(userDto.getName()), String.class))
                .andExpect(jsonPath("$.[0].email", is(userDto.getEmail()), String.class));
    }

    @Test
    @DisplayName("Delete users по id")
    public void testDelete() throws Exception {
        mockMvc.perform(delete("/admin/users/" + userDto.getId()))
                .andExpect(status().isNoContent());
    }
}