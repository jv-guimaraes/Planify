package com.planify.planify.dtos.transaction;

import com.planify.planify.dtos.category.CategoryShortResponseDto;
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
        CategoryShortResponseDto category,
        TransactionStatus status
) {
}
