package ru.practicum.dto.compilation;

import lombok.Data;

@Data
public class ParamCompilationFindAll {

    private final Boolean pinned;
    private final int from;
    private final int size;
}
