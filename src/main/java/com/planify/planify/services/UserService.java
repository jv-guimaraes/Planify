package com.planify.planify.services;

import com.planify.planify.dtos.UserRequestDto;
import com.planify.planify.entities.Category;
import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.User;
import com.planify.planify.repositories.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository,
                       @Lazy TransactionService transactionService,
                       @Lazy CategoryService categoryService,
                       @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.categoryService = categoryService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean registerUser(UserRequestDto userDto) {
        if (userDto.username() == null) return false;
        if (userDto.email() == null) return false;
        if (userDto.password() == null) return false;
        User user = new User();
        user.setUsername(userDto.username());
        user.setEmail(userDto.email());
        user.setPassword(passwordEncoder.encode(userDto.password()));
        userRepository.save(user);
        return true;
    }

    //    ------------------ CRUD ------------------
    public UUID createUser(UserRequestDto userDto) {
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
        User user = userRepository.findById(userId).orElseThrow();
        for (Transaction t : user.getTransactions()) {
            transactionService.deleteById(t.getTransactionId());
        }
        for (Category c : user.getCategories()) {
            categoryService.deleteById(c.getCategoryId());
        }
        userRepository.deleteById(userId);
    }

    public Optional<User> updateById(UUID userId, UserRequestDto userDto) {
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
