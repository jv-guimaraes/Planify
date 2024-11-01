package com.planify.planify.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.planify.planify.dtos.GoalRequestDto;
import com.planify.planify.dtos.GoalResponseDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tb_goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "goal_id")
    private UUID goalId;

    @Column(name = "name")
    private String name;

    @Column(name = "target_amount")
    private BigDecimal targetAmount;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @ManyToOne
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category category;

    @OneToMany(mappedBy = "goal")
    @JsonManagedReference
    private List<Transaction> transactions;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;

    public BigDecimal getCurrentAmount() {
        var total = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            total = total.add(transaction.getValue());
        }
        return total;
    }

    public GoalResponseDto toResponseDto() {
        return new GoalResponseDto(
                name,
                targetAmount,
                targetDate,
                category.toResponseDto(),
                transactions.stream().map(Transaction::getTransactionId).toList(),
                getCurrentAmount()
        );
    }
}
