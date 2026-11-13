package ru.practicum.service;

import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.ParamUserFindAll;
import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {

    UserDto save(NewUserRequest newUserRequest);

    void delete(Long id);

    List<UserDto> findAll(ParamUserFindAll params);

    UserDto findById(Long id);
}
