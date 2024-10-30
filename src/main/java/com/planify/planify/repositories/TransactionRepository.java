package com.planify.planify.repositories;

import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.TransactionStatus;
import com.planify.planify.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUser(User user);

    List<Transaction> findByUserOrderByDate(User user);

    List<Transaction> findByUserAndDateBetweenOrderByDate(User user, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByStatusAndDateBefore(TransactionStatus transactionStatus, LocalDate now);

    List<Transaction> findByUserAndStatusAndDateBetweenOrderByDate(User user, TransactionStatus status, LocalDate startDate, LocalDate endDate);
}
