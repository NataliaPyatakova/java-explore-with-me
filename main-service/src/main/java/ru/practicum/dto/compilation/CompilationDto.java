package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.dto.event.EventShortDto;

import java.util.List;

@Data
@Accessors(chain = true)
public class CompilationDto {

    @NotNull
    private Long id;
    @NotBlank
    private String title;
    @NotNull
    private Boolean pinned;
    private List<EventShortDto> events;
}
