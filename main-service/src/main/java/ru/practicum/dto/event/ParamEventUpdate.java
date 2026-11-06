package ru.practicum.dto.event;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.model.Category;

@Data
@Accessors(chain = true)
public class ParamEventUpdate {

    private Category category;
}
