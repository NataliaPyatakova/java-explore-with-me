package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.ParamCategoryFindAll;
import ru.practicum.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping(path = "/categories")
@Slf4j
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> findAll(@RequestParam(value = "from", defaultValue = "0") Integer from,
                                     @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("PublicCategoryController findAll from {} size {}", from, size);
        ParamCategoryFindAll params = new ParamCategoryFindAll(from, size);
        return categoryService.findAll(params);
    }

    @GetMapping("/{catId}")
    public CategoryDto findById(@PathVariable Long catId) {
        log.info("PublicCategoryController findById categoryId {}", catId);
        return categoryService.findById(catId);
    }
}
