package ru.practicum.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LocationDto {

    @NotNull
    private Double lat;
    @NotNull
    private Double lon;
}
