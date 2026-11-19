package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.enumeration.RequestStatus;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ParticipationRequestDto {

    @NotNull
    private Long id;
    @NotNull
    private Long event;
    @NotNull
    private Long requester;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
    private RequestStatus status = RequestStatus.PENDING;
}
