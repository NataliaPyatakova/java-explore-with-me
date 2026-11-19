package ru.practicum.dto.compilation;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class UpdateCompilationRequest {

    @Size(min = 1, max = 50, message = "Title must be between 3 and 120 characters")
    private String title;
    private Boolean pinned;
    private List<Long> events;
}
