package com.planify.planify.services;

import com.planify.planify.controllers.CategoryDto;
import com.planify.planify.entities.Category;
import com.planify.planify.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public UUID createCategory(CategoryDto categoryDto) {
        Category category = new Category(UUID.randomUUID(), categoryDto.name(), new ArrayList<>());
        categoryRepository.save(category);
        return category.getCategoryId();
    }

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public void deleteById(UUID categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    public Optional<Category> getById(UUID categoryId) {
        return categoryRepository.findById(categoryId);
    }

    public Optional<Category> updateById(UUID id, CategoryDto dto) {
        var res = categoryRepository.findById(id);
        if (res.isPresent()) {
            var category = res.get();
            if (dto.name() != null) category.setName(dto.name());
            categoryRepository.save(category);
            return Optional.of(category);
        } else {
            return Optional.empty();
        }
    }
}
