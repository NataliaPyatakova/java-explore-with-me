package ru.practicum.dto.event;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.model.Category;
import ru.practicum.model.Location;
import ru.practicum.model.User;

@Data
@Accessors(chain = true)
public class ParamEventSave {

    private final User user;
    private final Category category;
    private final Location location;
}
