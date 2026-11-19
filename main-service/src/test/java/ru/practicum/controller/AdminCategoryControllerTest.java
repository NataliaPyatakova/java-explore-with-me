package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.service.CategoryService;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminCategoryController.class)
public class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private CategoryService categoryService;

    private final NewCategoryDto newCategoryDto = new NewCategoryDto().setName("test_name");
    private final CategoryDto categoryDto = new CategoryDto().setId(1L).setName("test_name");
    private final CategoryDto categoryDtoForUpdate = new CategoryDto().setId(1L).setName("update_name");
    private final CategoryDto updatedCategoryDto = new CategoryDto().setId(1L).setName("update_name");

    @Test
    @DisplayName("Post category - все хорошо")
    void testSaveOk() throws Exception {
        when(categoryService.save(newCategoryDto)).thenReturn(categoryDto);
        mockMvc.perform(post("/admin/categories")
                        .content(mapper.writeValueAsString(newCategoryDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(categoryDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(categoryDto.getName())));
    }

    @Test
    @DisplayName("Post category - name null")
    void testSaveNullName() throws Exception {
        newCategoryDto.setName(null);
        mockMvc.perform(post("/admin/categories")
                        .content(mapper.writeValueAsString(newCategoryDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post category - name пустое")
    void testSaveEmptyName() throws Exception {
        newCategoryDto.setName("");
        mockMvc.perform(post("/admin/categories")
                        .content(mapper.writeValueAsString(newCategoryDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post category - name из пробелов")
    void testSaveBlankName() throws Exception {
        newCategoryDto.setName("    ");
        mockMvc.perform(post("/admin/categories")
                        .content(mapper.writeValueAsString(newCategoryDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Post category - name больше 50")
    void testSaveName50() throws Exception {
        newCategoryDto.setName("ф".repeat(51));
        mockMvc.perform(post("/admin/categories")
                        .content(mapper.writeValueAsString(newCategoryDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Delete category по id")
    public void testDelete() throws Exception {
        mockMvc.perform(delete("/admin/categories/" + categoryDto.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Patch category - все хорошо")
    void testPatchOk() throws Exception {
        when(categoryService.update(categoryDtoForUpdate.getId(), categoryDtoForUpdate)).thenReturn(updatedCategoryDto);
        mockMvc.perform(patch("/admin/categories/" + categoryDtoForUpdate.getId())
                        .content(mapper.writeValueAsString(categoryDtoForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedCategoryDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updatedCategoryDto.getName())));
    }

    @Test
    @DisplayName("Patch category - name пустое")
    void testPatchEmptyName() throws Exception {
        categoryDtoForUpdate.setName("");
        mockMvc.perform(patch("/admin/categories/" + categoryDtoForUpdate.getId())
                        .content(mapper.writeValueAsString(categoryDtoForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch category - name из пробелов")
    void testPatchBlankName() throws Exception {
        categoryDtoForUpdate.setName("    ");
        mockMvc.perform(patch("/admin/categories/" + categoryDtoForUpdate.getId())
                        .content(mapper.writeValueAsString(categoryDtoForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Patch category - name больше 50")
    void testPatchName50() throws Exception {
        categoryDtoForUpdate.setName("ф".repeat(51));
        mockMvc.perform(patch("/admin/categories/" + categoryDtoForUpdate.getId())
                        .content(mapper.writeValueAsString(categoryDtoForUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Delete category по id")
    public void testDeleteError() throws Exception {
        mockMvc.perform(delete("/admin/categories/a"))
                .andExpect(status().isBadRequest());
    }
}