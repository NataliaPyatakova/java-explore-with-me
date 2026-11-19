package ru.practicum.dto.user;

import lombok.Data;

import java.util.List;

@Data
public class ParamUserFindAll {

    private final List<Long> ids;
    private final int from;
    private final int size;
}
