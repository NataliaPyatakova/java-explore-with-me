package ru.practicum.dto.event;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.enumeration.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class ParamEventsFindByAdmin {

    private List<Long> users;
    private List<EventState> states;
    private List<Long> categories;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Integer from;
    private Integer size;
}
