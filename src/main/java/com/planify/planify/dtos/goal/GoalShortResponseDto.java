package com.planify.planify.dtos.goal;

import java.util.UUID;

public record GoalShortResponseDto(
        UUID id,
        String name
) {
}
