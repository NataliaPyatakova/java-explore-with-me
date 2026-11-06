package ru.practicum.dto.event;

import lombok.Data;

@Data
public class ParamEventFindByUser {

    private final Long userId;
    private final int from;
    private final int size;
}
