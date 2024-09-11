package com.planify.planify.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionDto(
        LocalDate date,
        String sender,
        String recipient,
        BigDecimal value,
        boolean isExpense,
        UUID user,
        UUID category
) {
}
