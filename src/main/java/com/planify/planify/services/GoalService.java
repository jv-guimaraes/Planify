package com.planify.planify.services;

import com.planify.planify.dtos.goal.GoalContributionDto;
import com.planify.planify.dtos.goal.GoalRequestDto;
import com.planify.planify.dtos.transaction.TransactionRequestDto;
import com.planify.planify.entities.Goal;
import com.planify.planify.entities.TransactionStatus;
import com.planify.planify.repositories.CategoryRepository;
import com.planify.planify.repositories.GoalRepository;
import com.planify.planify.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoalService {
    private final UserRepository userRepository;
    private final GoalRepository goalRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionService transactionService;

    public GoalService(UserRepository userRepository, GoalRepository goalRepository, CategoryRepository categoryRepository, TransactionService transactionService) {
        this.userRepository = userRepository;
        this.goalRepository = goalRepository;
        this.categoryRepository = categoryRepository;
        this.transactionService = transactionService;
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

    public Optional<Goal> findById(UUID id) {
        return goalRepository.findById(id);
    }

    public ResponseEntity<String> updateBalance(Principal principal, UUID id, GoalContributionDto contribution) {
        var user = userRepository.findByEmail(principal.getName()).orElseThrow();
        var goal = goalRepository.findById(id);

        if (goal.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Goal was not found.");
        if (contribution.value().compareTo(BigDecimal.ZERO) <= 0)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Contribution has to be a positive number.");

        var transaction = new TransactionRequestDto(
                LocalDate.now(),
                user.getUsername(),
                goal.get().getName(),
                contribution.value(),
                true,
                goal.get().getCategory().getCategoryId(),
                TransactionStatus.COMPLETE,
                true,
                id
        );
        transactionService.createTransaction(user.getUserId(), transaction);

        var msg = String.format("Goal '%s' was updated successfully!", goal.get().getName());
        return ResponseEntity.status(HttpStatus.OK).body(msg);
    }
}
