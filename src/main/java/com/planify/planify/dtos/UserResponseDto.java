package com.planify.planify.dtos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponseDto(
        UUID userId,
        String username,
        String email,
        List<TransactionResponseDto> transactions,
        List<CategoryResponseDto> categories,
        List<GoalResponseDto> goals,
        Instant creationTimestamp,
        Instant updateTimestamp
) {
}
