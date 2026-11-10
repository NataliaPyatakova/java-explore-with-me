package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.dto.category.ParamCategoryFindAll;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto save(NewCategoryDto newCategoryDto) {
        log.info("save category {}", newCategoryDto);
        Category category = categoryRepository.save(CategoryMapper.mapToCategory(newCategoryDto));
        return CategoryMapper.mapToCategoryDto(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("delete category id {}", id);
        findCategoryById(id);
        List<Event> list = eventRepository.findAllByCategoryId(id);
        if (!list.isEmpty()) {
            throw new ValidationException("Validation Exception", "There is an event with the same category id");
        }
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        log.info("update category id {} categoryDto {}", id, categoryDto);
        Category category = findCategoryById(id);
        category.setName(categoryDto.getName());
        return CategoryMapper.mapToCategoryDto(category);
    }

    @Override
    public CategoryDto findById(Long id) {
        log.info("find category id {}", id);
        return CategoryMapper.mapToCategoryDto(findCategoryById(id));
    }

    @Override
    public List<CategoryDto> findAll(ParamCategoryFindAll params) {
        log.info("findAll params {}", params);
        //в требованиях не нашла - сортиурем по Id чтобы не кучей
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        Pageable pageable = PageRequest.of(params.getFrom(), params.getSize(), sortById);
        Page<Category> page = categoryRepository.findAll(pageable);
        return page.getContent().stream().map(CategoryMapper::mapToCategoryDto).toList();
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found",
                        "The required object was not found."));
    }
}