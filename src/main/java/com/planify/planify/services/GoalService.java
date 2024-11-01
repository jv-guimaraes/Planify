package com.planify.planify.services;

import com.planify.planify.dtos.goal.GoalRequestDto;
import com.planify.planify.entities.Goal;
import com.planify.planify.repositories.CategoryRepository;
import com.planify.planify.repositories.GoalRepository;
import com.planify.planify.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoalService {
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final CategoryRepository categoryRepository;

    public GoalService(UserRepository userRepository, GoalRepository goalRepository, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.goalRepository = goalRepository;
        this.categoryRepository = categoryRepository;
    }

    public Optional<Goal> createGoal(UUID userId, GoalRequestDto dto) {
        var user = userRepository.findById(userId);
        var category = categoryRepository.findById(dto.category());
        if (user.isEmpty() || category.isEmpty()) return Optional.empty();

        Goal goal = new Goal(
                null,
                dto.name(),
                dto.targetAmount(),
                dto.targetDate(),
                category.get(),
                new ArrayList<>(),
                user.get()
        );
        goalRepository.save(goal);

        return Optional.of(goal);
    }

}
