package com.planify.planify.controllers;

import com.planify.planify.entities.Category;
import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.User;
import com.planify.planify.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDto dto) {
        var id = userService.createUser(dto);
        return ResponseEntity.created(URI.create("v1/users/" + id.toString())).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.of(userService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateById(@PathVariable("id") UUID id, @RequestBody UserDto dto) {
        return ResponseEntity.of(userService.updateById(id, dto));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable("id") UUID id) {
        return ResponseEntity.of(userService.getTransactionsByUserId(id));
    }

    @GetMapping("/{id}/categories")
    public ResponseEntity<List<Category>> getCategories(@PathVariable("id") UUID id) {
        return ResponseEntity.of(userService.getCategoriesByUserId(id));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        userService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
