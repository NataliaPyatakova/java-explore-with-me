package ru.practicum.dto.event;

import lombok.Data;

@Data
public class ParamEventDto {

    private final Long confirmedRequests;
    private final Long views;
}
