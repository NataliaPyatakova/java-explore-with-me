package ru.practicum.dto.category;

import lombok.Data;

@Data
public class ParamCategoryFindAll {

    private final int from;
    private final int size;
}
