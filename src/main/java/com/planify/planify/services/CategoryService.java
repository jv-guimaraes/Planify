package com.planify.planify.services;

import com.planify.planify.dtos.CategoryRequestDto;
import com.planify.planify.entities.Category;
import com.planify.planify.entities.User;
import com.planify.planify.repositories.CategoryRepository;
import com.planify.planify.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public CategoryService(UserRepository userRepository, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public Optional<Category> createCategory(UUID userId, CategoryRequestDto dto) {
        var user = userRepository.findById(userId);
        if (user.isPresent()) {
            Category category = new Category(null, dto.name(), new ArrayList<>(), user.get());
            categoryRepository.save(category);
            return Optional.of(category);
        } else {
            return Optional.empty();
        }
    }

    public void deleteById(UUID categoryId) {
        categoryRepository.deleteById(categoryId);
    }

    public Optional<Category> findById(UUID categoryId) {
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

    public List<Category> findByUser(User user) {
        return categoryRepository.findByUser(user);
    }
}
