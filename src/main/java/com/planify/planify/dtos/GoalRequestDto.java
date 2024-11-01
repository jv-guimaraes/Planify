package com.planify.planify.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoalRequestDto(
        String name,
        BigDecimal targetAmount,
        LocalDate targetDate,
        UUID category
) {
}
