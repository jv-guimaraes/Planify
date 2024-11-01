package com.planify.planify.controllers;

import com.planify.planify.dtos.GoalRequestDto;
import com.planify.planify.dtos.GoalResponseDto;
import com.planify.planify.dtos.TransactionResponseDto;
import com.planify.planify.entities.Goal;
import com.planify.planify.entities.Transaction;
import com.planify.planify.repositories.GoalRepository;
import com.planify.planify.repositories.UserRepository;
import com.planify.planify.services.GoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

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
}
