package ru.practicum.service;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.category.ParamCategoryFindAll;

import java.util.List;

public interface CategoryService {

    CategoryDto save(NewCategoryDto newCategoryDto);

    void delete(Long id);

    CategoryDto update(Long id, CategoryDto categoryDto);

    CategoryDto findById(Long id);

    List<CategoryDto> findAll(ParamCategoryFindAll params);
}
