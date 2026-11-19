package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.ParamUserFindAll;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;

import java.util.ArrayList;
import java.util.List;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private final NewUserRequest newUserRequest = new NewUserRequest()
            .setEmail("test_email")
            .setName("test_name");
    private final NewUserRequest newUserRequest1 = new NewUserRequest()
            .setEmail("test_email1")
            .setName("test_name1");
    private final NewUserRequest newUserRequest2 = new NewUserRequest()
            .setEmail("test_email2")
            .setName("test_name2");
    private final List<Long> ids = new ArrayList<>();

    @Test
    @DisplayName("Сохранение user - все хорошо")
    public void testSave() {
        UserDto result = userService.save(newUserRequest);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newUserRequest.getEmail(), result.getEmail());
        Assertions.assertEquals(newUserRequest.getName(), result.getName());
    }

    @Test
    @DisplayName("Сохранение user - дублирующий email")
    public void testSaveDuplicateEmail() {
        userService.save(newUserRequest);
        try {
            userService.save(newUserRequest);
        } catch (final DataIntegrityViolationException e) {
            Assertions.assertTrue(e.getMessage().contains("Unique index or primary key violation"));
        }
    }

    @Test
    @DisplayName("Поиск user - правильный id")
    public void testFindByIDRightId() {
        UserDto userDto = userService.save(newUserRequest);
        UserDto result = userService.findById(userDto.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newUserRequest.getEmail(), result.getEmail());
        Assertions.assertEquals(newUserRequest.getName(), result.getName());
    }

    @Test
    @DisplayName("Поиск user - неправильный id")
    public void testFindByIDWrongId() {
        userService.save(newUserRequest);
        try {
            userService.findById(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Удаление user - правильный id")
    public void testDeleteRightId() {
        UserDto userDto = userService.save(newUserRequest);
        userService.delete(userDto.getId());
        try {
            userService.findById(userDto.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=" + userDto.getId() + " was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Удаление user - неправильный id")
    public void testDeleteWrongId() {
        userService.save(newUserRequest);
        try {
            userService.delete(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("User with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск user - пустой список - без id, from=0, size = 10")
    void testFindAllOkNoUsers() {
        ParamUserFindAll params = new ParamUserFindAll(ids, 0, 10);
        List<UserDto> result = userService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск user - без id, from=0, size = 10")
    void testFindAllOkNoIdDefaultParam() {
        UserDto userDto = userService.save(newUserRequest);
        ParamUserFindAll params = new ParamUserFindAll(ids, 0, 10);
        List<UserDto> result = userService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(userDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(userDto.getEmail(), result.getFirst().getEmail());
        Assertions.assertEquals(userDto.getName(), result.getFirst().getName());
    }

    @Test
    @DisplayName("Поиск user - по конкретному id, from=0, size = 10")
    void testFindAllOkWithIdDefaultParam() {
        UserDto userDto = userService.save(newUserRequest);
        ids.add(userDto.getId());
        ParamUserFindAll params = new ParamUserFindAll(ids, 0, 10);
        List<UserDto> result = userService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(ids.size(), result.size());
        Assertions.assertEquals(userDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(userDto.getEmail(), result.getFirst().getEmail());
        Assertions.assertEquals(userDto.getName(), result.getFirst().getName());
    }

    @Test
    @DisplayName("Поиск user - по конкретным id, from=0, size = 2")
    void testFindAllOkWithIdFrom0Size2() {
        UserDto userDto = userService.save(newUserRequest);
        UserDto userDto1 = userService.save(newUserRequest1);
        UserDto userDto2 = userService.save(newUserRequest2);
        ids.add(userDto.getId());
        ids.add(userDto1.getId());
        ids.add(userDto2.getId());
        ParamUserFindAll params = new ParamUserFindAll(ids, 0, 2);
        List<UserDto> result = userService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(3, result.size()); //игнорируем параметры
    }

    @Test
    @DisplayName("Поиск user - без id, from=0, size = 2")
    void testFindAllOkNoIdFrom0Size2() {
        userService.save(newUserRequest);
        userService.save(newUserRequest1);
        userService.save(newUserRequest2);
        ParamUserFindAll params = new ParamUserFindAll(ids, 0, 2);
        List<UserDto> result = userService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size()); //список пустой - смотрим параметры
    }
}