package com.planify.planify.dtos.user;

import com.planify.planify.dtos.category.CategoryShortResponseDto;
import com.planify.planify.dtos.goal.GoalShortResponseDto;
import com.planify.planify.dtos.transaction.TransactionResponseDto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponseDto(
        UUID userId,
        String username,
        String email,
        List<TransactionResponseDto> transactions,
        List<CategoryShortResponseDto> categories,
        List<GoalShortResponseDto> goals,
        Instant creationTimestamp,
        Instant updateTimestamp
) {
}
