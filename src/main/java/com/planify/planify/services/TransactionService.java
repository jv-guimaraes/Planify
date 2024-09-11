package com.planify.planify.services;

import com.planify.planify.controllers.TransactionDto;
import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.User;
import com.planify.planify.repositories.CategoryRepository;
import com.planify.planify.repositories.TransactionRepository;
import com.planify.planify.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public Optional<Transaction> createTransaction(TransactionDto dto) {
        var userRes = userRepository.findById(dto.user());
        var categoryRes = categoryRepository.findById(dto.category());
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
