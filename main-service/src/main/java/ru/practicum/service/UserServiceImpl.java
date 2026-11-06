package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequest;
import ru.practicum.dto.user.ParamUserFindAll;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto save(NewUserRequest newUserRequest) {
        log.info("save user {}", newUserRequest);
        User user = userRepository.save(UserMapper.mapToUser(newUserRequest));
        return UserMapper.mapToUserDto(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("delete user id {}", id);
        findUserById(id);
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> findAll(ParamUserFindAll params) {
        log.info("findAll users params {}", params);
        //Возвращает информацию обо всех пользователях (учитываются параметры ограничения выборки),
        //либо о конкретных (учитываются указанные идентификаторы)
        List<User> userList;
        if (params.getIds() == null || params.getIds().isEmpty()) {
            //в требованиях не нашла - сортиурем по Id чтобы не кучей
            Sort sortById = Sort.by(Sort.Direction.ASC, "id");
            Pageable pageable = PageRequest.of(params.getFrom(), params.getSize(), sortById);
            Page<User> page = userRepository.findAll(pageable);
            userList = page.getContent();
        } else {
            userList = userRepository.findAllById(params.getIds());
        }
        return userList.stream().map(UserMapper::mapToUserDto).toList();
    }

    @Override
    public UserDto findById(Long id) {
        log.info("findById user id {}", id);
        return UserMapper.mapToUserDto(findUserById(id));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id=" + id + " was not found",
                        "The required object was not found."));
    }
}