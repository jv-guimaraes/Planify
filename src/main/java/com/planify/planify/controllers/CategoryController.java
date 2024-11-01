package com.planify.planify.controllers;

import com.planify.planify.dtos.CategoryRequestDto;
import com.planify.planify.entities.Category;
import com.planify.planify.services.CategoryService;
import com.planify.planify.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("v1/categories")
public class CategoryController {
    private final CategoryService categoryService;
    private final UserService userService;

    public CategoryController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(Principal principal, @RequestBody CategoryRequestDto dto) {
        var user = userService.findByEmail(principal.getName()).orElseThrow();
        var category = categoryService.createCategory(user.getUserId(), dto);
        return ResponseEntity.of(category);
    }

    @GetMapping("{id}")
    public ResponseEntity<Category> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.of(categoryService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAll(Principal principal) {
        var user = userService.findByEmail(principal.getName()).orElseThrow();
        return ResponseEntity.ok(categoryService.findByUser(user));
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
