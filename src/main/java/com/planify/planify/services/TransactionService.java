package com.planify.planify.services;

import com.planify.planify.dtos.TransactionRequestDto;
import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.User;
import com.planify.planify.repositories.TransactionRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public TransactionService(TransactionRepository transactionRepository, @Lazy UserService userService, @Lazy CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    public Optional<Transaction> createTransaction(TransactionRequestDto dto) {
        var userRes = userService.getById(dto.user());
        var categoryRes = categoryService.getById(dto.category());
        if (userRes.isPresent() && categoryRes.isPresent()) {
            var user = userRes.get();
            var category = categoryRes.get();

            var transaction = new Transaction();
            transaction.setDate(dto.date());
            transaction.setSender(dto.sender());
            transaction.setRecipient(dto.recipient());
            transaction.setValue(dto.value());
            transaction.setExpense(dto.isExpense());
            transaction.setUser(user);
            transaction.setCategory(category);
            transactionRepository.save(transaction);

            return Optional.of(transaction);
        } else {
            return Optional.empty();
        }
    }

    public List<Transaction> getAll() {
        return transactionRepository.findAll();
    }

    public void deleteAll() {
        transactionRepository.deleteAll();
    }

    public List<Transaction> getByUser(User user) {
        return transactionRepository.findByUser(user);
    }
}
