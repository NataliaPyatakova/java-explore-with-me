package ru.practicum.dto.event;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;

@Data
@Accessors(chain = true)
public class ParamEventUpdate {

    private Event event;
    private Category category;
    private Location location;
}
