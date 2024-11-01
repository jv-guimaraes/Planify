package com.planify.planify.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GoalResponseDto(
        String name,
        BigDecimal targetAmount,
        LocalDate targetDate,
        CategoryResponseDto category,
        List<UUID> transactions,
        BigDecimal currentAmount
) {
}
