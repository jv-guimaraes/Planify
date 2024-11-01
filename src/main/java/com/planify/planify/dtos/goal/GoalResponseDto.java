package com.planify.planify.dtos.goal;

import com.planify.planify.dtos.category.CategoryShortResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GoalResponseDto(
        String name,
        BigDecimal targetAmount,
        LocalDate targetDate,
        CategoryShortResponseDto category,
        List<UUID> transactions,
        BigDecimal currentAmount
) {
}
