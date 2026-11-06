package ru.practicum.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.ParamCategoryFindAll;
import ru.practicum.service.CategoryService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PublicCategoryController.class)
public class PublicCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    private final CategoryDto categoryDto = new CategoryDto().setId(1L).setName("test_name");
    List<CategoryDto> categoryDtoList = new ArrayList<>();

    @Test
    @DisplayName("Get user - все хорошо")
    void testFindByIdAllOk() throws Exception {
        when(categoryService.findById(categoryDto.getId())).thenReturn(categoryDto);
        mockMvc.perform(get("/categories/" + categoryDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(categoryDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(categoryDto.getName()), String.class));
    }

    @Test
    @DisplayName("Get category - некорректный параметр")
    void testFindByIdError400() throws Exception {
        mockMvc.perform(get("/categories/a"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get all categories - все параметры - все хорошо")
    void testFindAllOk() throws Exception {
        categoryDtoList.add(categoryDto);
        ParamCategoryFindAll params = new ParamCategoryFindAll(0, 10);
        when(categoryService.findAll(params)).thenReturn(categoryDtoList);
        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(categoryDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(categoryDto.getName()), String.class));
    }

    @Test
    @DisplayName("Get all categories - без from - все хорошо")
    void testFindAllWithOutFromOk() throws Exception {
        categoryDtoList.add(categoryDto);
        ParamCategoryFindAll params = new ParamCategoryFindAll(0, 10);
        when(categoryService.findAll(params)).thenReturn(categoryDtoList);
        mockMvc.perform(get("/categories")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(categoryDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(categoryDto.getName()), String.class));
    }

    @Test
    @DisplayName("Get all categories - без size - все хорошо")
    void testFindAllWithOutSizeOk() throws Exception {
        categoryDtoList.add(categoryDto);
        ParamCategoryFindAll params = new ParamCategoryFindAll(0, 10);
        when(categoryService.findAll(params)).thenReturn(categoryDtoList);
        mockMvc.perform(get("/categories")
                        .param("from", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(categoryDto.getId()), Long.class))
                .andExpect(jsonPath("$.[0].name", is(categoryDto.getName()), String.class));
    }
}