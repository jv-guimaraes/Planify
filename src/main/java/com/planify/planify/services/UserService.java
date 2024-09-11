package com.planify.planify.services;

import com.planify.planify.dtos.UserDto;
import com.planify.planify.entities.Category;
import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.User;
import com.planify.planify.repositories.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    public UserService(UserRepository userRepository, @Lazy TransactionService transactionService, @Lazy CategoryService categoryService) {
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    public UUID createUser(UserDto userDto) {
        var user = new User(
                UUID.randomUUID(),
                userDto.username(),
                userDto.password(),
                userDto.email(),
                new ArrayList<>(),
                new ArrayList<>(),
                Instant.now(),
                null);
        return userRepository.save(user).getUserId();
    }

    public Optional<User> getById(UUID userId) {
        return userRepository.findById(userId);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public void deleteById(UUID userId) {
        userRepository.deleteById(userId);
    }

    public Optional<User> updateById(UUID userId, UserDto userDto) {
        var result = userRepository.findById(userId);
        if (result.isPresent()) {
            var user = result.get();
            if (userDto.username() != null) user.setUsername(userDto.username());
            if (userDto.password() != null) user.setPassword(userDto.password());
            if (userDto.email() != null) user.setEmail(userDto.email());
            userRepository.save(user);
            return Optional.of(user);
        } else {
            return Optional.empty();
        }
    }

    public Optional<List<Transaction>> getTransactionsByUserId(UUID id) {
        var user = userRepository.findById(id);
        return user.map(transactionService::getByUser);
    }

    public Optional<List<Category>> getCategoriesByUserId(UUID id) {
        var user = userRepository.findById(id);
        return user.map(categoryService::getByUser);
    }

    public void deleteAll() {
        transactionService.deleteAll();
        categoryService.deleteAll();
        userRepository.deleteAll();
    }
}
