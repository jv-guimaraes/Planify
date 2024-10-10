package com.planify.planify.services;

import com.planify.planify.dtos.TransactionRequestDto;
import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.User;
import com.planify.planify.repositories.TransactionRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public ByteArrayResource exportCsv(Principal principal, LocalDate startDate, LocalDate endDate) {
        User user = userService.findByEmail(principal.getName()).orElseThrow();
        List<Transaction> transactions;
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserAndDateBetweenOrderByDate(user, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserOrderByDate(user);
        }

        // Gerar o CSV
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        writer.println("date,sender,recipient,value,is_expense,category");
        transactions.stream().map(Transaction::toResponseDto).forEach(t -> {
            var type = t.isExpense() ? "expense" : "income";
            writer.printf("%s,%s,%s,\"%.2f\",%s,%s\n", t.date(), t.sender(), t.recipient(),
                    t.value(), type, t.category().name());
        });
        writer.flush();
        return new ByteArrayResource(outputStream.toByteArray());
    }

    public Optional<Transaction> createTransaction(UUID userId, TransactionRequestDto dto) {
        var userRes = userService.getById(userId);
        var categoryRes = categoryService.getById(dto.category());
        System.out.println(dto);
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

    public Optional<Transaction> findById(UUID id) {
        return transactionRepository.findById(id);
    }

    public boolean deleteById(UUID id) {
        if (transactionRepository.findById(id).isPresent()) {
            transactionRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }
}
