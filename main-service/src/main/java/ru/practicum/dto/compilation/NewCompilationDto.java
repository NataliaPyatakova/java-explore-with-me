package ru.practicum.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class NewCompilationDto {

    @NotBlank
    @Size(min = 1, max = 50, message = "Title must be between 1 and 50 characters")
    private String title;
    private Boolean pinned = false;
    private List<Long> events;
}
