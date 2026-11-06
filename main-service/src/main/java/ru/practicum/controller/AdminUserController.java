package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.ParamUserFindAll;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@Slf4j
@RequiredArgsConstructor
@Validated
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public List<UserDto> findAll(@RequestParam(value = "ids", required = false) List<Long> ids,
                                 @RequestParam(value = "from", defaultValue = "0") Integer from,
                                 @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("AdminUserController findAll ids {} from {} size {}", ids, from, size);
        ParamUserFindAll paramUserFindAll = new ParamUserFindAll(ids, from, size);
        return userService.findAll(paramUserFindAll);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto save(@RequestBody @Valid NewUserRequest newUserRequest) {
        log.info("AdminUserController save user {}", newUserRequest);
        return userService.save(newUserRequest);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long userId) {
        log.info("AdminUserController delete userId {}", userId);
        userService.delete(userId);
    }
}