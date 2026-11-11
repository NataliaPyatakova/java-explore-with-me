package ru.practicum.dto.event;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import ru.practicum.enumeration.Sort;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
@Accessors(chain = true)
public class ParamEventsFindByParam {

    private String text;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private Boolean onlyAvailable;
    private Sort sort;
    private Integer from;
    private Integer size;
}
