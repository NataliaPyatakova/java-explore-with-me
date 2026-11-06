package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.category.ParamCategoryFindAll;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    private final NewCategoryDto newCategoryDto = new NewCategoryDto().setName("test_name");
    private final NewCategoryDto newCategoryDto1 = new NewCategoryDto().setName("test_name1");
    private final NewCategoryDto newCategoryDto2 = new NewCategoryDto().setName("test_name2");
    private final CategoryDto updatedCategoryDto = new CategoryDto().setName("updated_name");

    @Test
    @DisplayName("Сохранение category - все хорошо")
    public void testSave() {
        CategoryDto result = categoryService.save(newCategoryDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newCategoryDto.getName(), result.getName());
    }

    @Test
    @DisplayName("Сохранение category - дублирующее имя")
    void testSaveDuplicateName() {
        categoryService.save(newCategoryDto);
        try {
            categoryService.save(newCategoryDto);
        } catch (final DataIntegrityViolationException e) {
            Assertions.assertTrue(e.getMessage().contains("Unique index or primary key violation"));
        }
    }

    @Test
    @DisplayName("Поиск category - правильный id")
    public void testFindByIDRightId() {
        CategoryDto categoryDto = categoryService.save(newCategoryDto);
        CategoryDto result = categoryService.findById(categoryDto.getId());
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newCategoryDto.getName(), result.getName());
    }

    @Test
    @DisplayName("Поиск category - неправильный id")
    public void testFindByIDWrongId() {
        categoryService.save(newCategoryDto);
        try {
            categoryService.findById(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Category with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Удаление category - правильный id")
    public void testDeleteRightId() {
        CategoryDto categoryDto = categoryService.save(newCategoryDto);
        categoryService.delete(categoryDto.getId());
        try {
            categoryService.findById(categoryDto.getId());
        } catch (NotFoundException e) {
            Assertions.assertEquals("Category with id=" + categoryDto.getId() + " was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Удаление category - неправильный id")
    public void testDeleteWrongId() {
        categoryService.save(newCategoryDto);
        try {
            categoryService.delete(999L);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Category with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Обновление category - правильный id")
    void testUpdateRightId() {
        CategoryDto categoryDto = categoryService.save(newCategoryDto);
        CategoryDto result = categoryService.update(categoryDto.getId(), updatedCategoryDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(updatedCategoryDto.getName(), result.getName());
    }

    @Test
    @DisplayName("Обновление category - неправильный id")
    void testUpdateWrongId() {
        categoryService.save(newCategoryDto);
        try {
            categoryService.update(999L, updatedCategoryDto);
        } catch (NotFoundException e) {
            Assertions.assertEquals("Category with id=999 was not found", e.getMessage());
            Assertions.assertEquals("The required object was not found.", e.getReason());
        }
    }

    @Test
    @DisplayName("Поиск category - пустой список - from=0, size = 10")
    void testFindNoCategoriesDefaultParam() {
        ParamCategoryFindAll params = new ParamCategoryFindAll(0, 10);
        List<CategoryDto> result = categoryService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Поиск category - from=0, size = 10")
    void testFindAllOkDefaultParam() {
        CategoryDto categoryDto = categoryService.save(newCategoryDto);
        ParamCategoryFindAll params = new ParamCategoryFindAll(0, 10);
        List<CategoryDto> result = categoryService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(categoryDto.getId(), result.getFirst().getId());
        Assertions.assertEquals(categoryDto.getName(), result.getFirst().getName());
    }

    @Test
    @DisplayName("Поиск category - from=0, size = 2")
    void testFindAllFrom0Size2() {
        categoryService.save(newCategoryDto);
        categoryService.save(newCategoryDto1);
        categoryService.save(newCategoryDto2);
        ParamCategoryFindAll params = new ParamCategoryFindAll(0, 2);
        List<CategoryDto> result = categoryService.findAll(params);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
    }
}