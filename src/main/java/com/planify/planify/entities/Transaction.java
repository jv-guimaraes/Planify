package com.planify.planify.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.planify.planify.dtos.transaction.TransactionRequestDto;
import com.planify.planify.dtos.transaction.TransactionResponseDto;
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

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.COMPLETE;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category category;

    @Column(name = "is_goal_contribution")
    private boolean isGoalContribution;

    @ManyToOne
    @JoinColumn(name = "goal_id")
    @JsonBackReference
    private Goal goal;

    public TransactionResponseDto toResponseDto() {
        return new TransactionResponseDto(
                transactionId,
                date,
                sender,
                recipient,
                value,
                isExpense,
                category.toShortResponseDto(),
                status,
                isGoalContribution
        );
    }

    public void update(TransactionRequestDto dto, Category category) {
        this.date = dto.date();
        this.sender = dto.sender();
        this.recipient = dto.recipient();
        this.value = dto.value();
        this.isExpense = dto.isExpense();
        this.category = category;
        this.status = dto.status();
    }
}
