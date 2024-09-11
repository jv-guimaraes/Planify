package com.planify.planify.repositories;

import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    public List<Transaction> findByUser(User user);
}
