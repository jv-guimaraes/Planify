package com.planify.planify.controllers;

import com.planify.planify.dtos.CategoryRequestDto;
import com.planify.planify.entities.Category;
import com.planify.planify.services.CategoryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("v1/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(@Lazy CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<UUID> createCategory(@RequestBody CategoryRequestDto dto) {
        var id = categoryService.createCategory(dto);
        return ResponseEntity.of(id);
    }

    @GetMapping("{id}")
    public ResponseEntity<Category> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.of(categoryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) {
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateById(@PathVariable("id") UUID id, @RequestBody CategoryRequestDto dto) {
        return ResponseEntity.of(categoryService.updateById(id, dto));
    }
}
