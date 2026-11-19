package ru.practicum.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.model.Category;

@SpringBootTest
public class CategoryMapperTest {

    private final Category category = new Category().setId(1L).setName("Category 1");
    private final NewCategoryDto newCategoryDto = new NewCategoryDto().setName("Category 1");
    private final CategoryDto categoryDto = new CategoryDto()
            .setId(1L)
            .setName("Category 1");

    @Test
    @DisplayName("Тест маппера из Category в CategoryDto")
    void testFromCategoryToCategoryDto() {
        CategoryDto result = CategoryMapper.mapToCategoryDto(category);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(category.getId(), result.getId());
        Assertions.assertEquals(category.getName(), result.getName());
    }

    @Test
    @DisplayName("Тест маппера из NewCategoryDto в Category")
    void testFromNewCategoryDtoToCategory() {
        Category result = CategoryMapper.mapToCategory(newCategoryDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(newCategoryDto.getName(), result.getName());
    }

    @Test
    @DisplayName("Тест маппера из CategoryDto в Category")
    void testFromCategoryDtoToCategory() {
        Category result = CategoryMapper.mapToCategoryFromCategoryDto(categoryDto);
        Assertions.assertNotNull(result);
        Assertions.assertEquals(categoryDto.getId(), result.getId());
        Assertions.assertEquals(categoryDto.getName(), result.getName());
    }
}