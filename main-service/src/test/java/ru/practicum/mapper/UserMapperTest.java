package ru.practicum.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.User;

@SpringBootTest
public class UserMapperTest {

    private final User user = new User()
            .setId(1L)
            .setEmail("test_email")
            .setName("test_name");
    private final NewUserRequest newUserRequest = new NewUserRequest()
            .setEmail("test_email")
            .setName("test_name");
    private final UserDto userDto = new UserDto()
            .setId(1L)
            .setEmail("test_email")
            .setName("test_name");
    private final UserShortDto userShortDto = new UserShortDto()
            .setId(1L)
            .setName("test_name");

    @Test
    @DisplayName("Тест маппера из User в UserDto")
    void testFromUserToUserDto() {
        UserDto result = UserMapper.mapToUserDto(user);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(user.getEmail(), result.getEmail());
        Assertions.assertEquals(user.getName(), result.getName());
    }

    @Test
    @DisplayName("Тест маппера из NewUserRequest в User")
    void testFromNewUserRequestToUser() {
        User result = UserMapper.mapToUser(newUserRequest);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newUserRequest.getEmail(), result.getEmail());
        Assertions.assertEquals(newUserRequest.getName(), result.getName());
    }

    @Test
    @DisplayName("Тест маппера из UserDto в User")
    void testFromUserDtoToUser() {
        User result = UserMapper.mapToUserFromUserDto(userDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(userDto.getId(), result.getId());
        Assertions.assertEquals(userDto.getEmail(), result.getEmail());
        Assertions.assertEquals(userDto.getName(), result.getName());
    }

    @Test
    @DisplayName("Тест маппера из UserShortDto в User")
    void testFromUserShortDtoToUser() {
        User result = UserMapper.mapToUserFromUserShortDto(userShortDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(userDto.getId(), result.getId());
        Assertions.assertEquals(userDto.getName(), result.getName());
    }

    @Test
    @DisplayName("Тест маппера из User в UserShortDto")
    void testFromUserToUserShortDto() {
        UserShortDto result = UserMapper.mapToUserShortDto(user);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(user.getId(), result.getId());
        Assertions.assertEquals(user.getName(), result.getName());
    }
}