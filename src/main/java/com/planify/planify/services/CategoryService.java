package com.planify.planify.services;

import com.planify.planify.dtos.CategoryRequestDto;
import com.planify.planify.entities.Category;
import com.planify.planify.entities.User;
import com.planify.planify.repositories.CategoryRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public CategoryService(CategoryRepository categoryRepository, @Lazy UserService userService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    public Optional<UUID> createCategory(CategoryRequestDto dto) {
        var user = userService.getById(dto.userId());
        if (user.isPresent()) {
            Category category = new Category(UUID.randomUUID(), dto.name(), new ArrayList<>(), user.get());
            categoryRepository.save(category);
            return Optional.of(category.getCategoryId());
        } else {
            return Optional.empty();
        }
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

    public Optional<Category> updateById(UUID id, CategoryRequestDto dto) {
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

    public List<Category> getByUser(User user) {
        return categoryRepository.findByUser(user);
    }

    public void deleteAll() {
        categoryRepository.deleteAll();
    }
}
