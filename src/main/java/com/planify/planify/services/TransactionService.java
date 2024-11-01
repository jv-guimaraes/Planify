package com.planify.planify.services;

import com.planify.planify.dtos.TransactionRequestDto;
import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.TransactionStatus;
import com.planify.planify.entities.User;
import com.planify.planify.repositories.CategoryRepository;
import com.planify.planify.repositories.TransactionRepository;
import com.planify.planify.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@EnableScheduling
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public Optional<Transaction> createTransaction(UUID userId, TransactionRequestDto dto) {
        var userRes = userRepository.findById(userId);
        var categoryRes = categoryRepository.findById(dto.category());
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
            transaction.setStatus(dto.status());
            transaction.setGoal(null);
            transaction.setGoalContribution(false);
            transactionRepository.save(transaction);
            return Optional.of(transaction);
        } else {
            return Optional.empty();
        }
    }

    public List<Transaction> findByUser(User user) {
        return transactionRepository.findByUser(user);
    }

    public List<Transaction> findByUserOrderByDate(User user) {
        return transactionRepository.findByUserOrderByDate(user);
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

    public Optional<Transaction> updateById(UUID id, TransactionRequestDto dto) {
        var res = transactionRepository.findById(id);
        if (res.isPresent()) {
            res.get().update(dto, categoryRepository.findById(dto.category()).orElseThrow());
            transactionRepository.save(res.get());
            return res;
        } else {
            return Optional.empty();
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")// Every day at midnight
    @PostConstruct
    private void updateTransactionStatus() {
        List<Transaction> pendingTransactions = transactionRepository.findByStatusAndDateBefore(
                TransactionStatus.PENDING, LocalDate.now()
        );
        for (Transaction transaction : pendingTransactions) {
            transaction.setStatus(TransactionStatus.COMPLETE);
            transactionRepository.save(transaction);
        }
        log.info("Updated {} pending transactions.", pendingTransactions.size());
    }
}
