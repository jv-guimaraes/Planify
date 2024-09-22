package com.planify.planify.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.planify.planify.dtos.CategoryResponseDto;
import com.planify.planify.dtos.TransactionResponseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "tb_transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "sender")
    private String sender;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "value")
    private BigDecimal value;

    @Column(name = "is_expense")
    private boolean isExpense;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category category;

    public TransactionResponseDto toResponseDto() {
        return new TransactionResponseDto(transactionId, date, sender, recipient, value, isExpense, category.toResponseDto());
    }

}
