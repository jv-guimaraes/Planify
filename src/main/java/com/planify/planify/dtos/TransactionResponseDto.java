package com.planify.planify.dtos;

import com.planify.planify.entities.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponseDto(
        UUID transactionId,
        LocalDate date,
        String sender,
        String recipient,
        BigDecimal value,
        boolean isExpense,
        CategoryResponseDto category,
        TransactionStatus status
) {
}
