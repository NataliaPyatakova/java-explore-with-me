package ru.practicum.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.enumeration.RequestStatus;

import java.util.List;

@Data
@Accessors(chain = true)
public class EventRequestStatusUpdateRequest {

    @NotNull
    private List<Long> requestIds;
    @NotNull
    private RequestStatus status;

}
