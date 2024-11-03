package com.planify.planify.controllers;

import com.planify.planify.dtos.goal.GoalContributionDto;
import com.planify.planify.dtos.goal.GoalRequestDto;
import com.planify.planify.dtos.goal.GoalResponseDto;
import com.planify.planify.dtos.transaction.TransactionRequestDto;
import com.planify.planify.entities.Goal;
import com.planify.planify.entities.TransactionStatus;
import com.planify.planify.repositories.UserRepository;
import com.planify.planify.services.GoalService;
import com.planify.planify.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("v1/goals")
public class GoalController {
    private final UserRepository userRepository;
    private final GoalService goalService;

    public GoalController(UserRepository userRepository, GoalService goalService) {
        this.userRepository = userRepository;
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<Goal> createGoal(Principal principal, @RequestBody GoalRequestDto dto) {
        var user = userRepository.findByEmail(principal.getName()).orElseThrow();
        var goal = goalService.createGoal(user.getUserId(), dto);
        return ResponseEntity.of(goal);
    }

    @GetMapping
    public ResponseEntity<List<GoalResponseDto>> getAll(Principal principal) {
        var user = userRepository.findByEmail(principal.getName()).orElseThrow();
        var goals = user.getGoals().stream().map(Goal::toResponseDto).toList();
        return ResponseEntity.ok(goals);
    }

    @PutMapping("/balance/{id}")
    public ResponseEntity<String> updateBalance(Principal principal, @PathVariable("id") UUID id, @RequestBody GoalContributionDto contribution) {
        return goalService.updateBalance(principal, id, contribution);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Goal> findById(Principal principal, @PathVariable("id") UUID id) {
        var user = userRepository.findByEmail(principal.getName()).orElseThrow();
        return ResponseEntity.of(goalService.findById(id));
    }
}
