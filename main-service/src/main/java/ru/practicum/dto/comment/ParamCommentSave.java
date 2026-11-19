package ru.practicum.dto.comment;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.model.Event;
import ru.practicum.model.User;

@Data
@Accessors(chain = true)
public class ParamCommentSave {

    private final User author;
    private final Event event;
}
