package ru.practicum.dto.event;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.practicum.enumeration.UserEventStateAction;

@Getter
@Setter
@Accessors(chain = true)
public class UpdateEventUserRequest extends UpdateEventRequest {

    private UserEventStateAction stateAction;

    public UpdateEventUserRequest() {
    }
}
