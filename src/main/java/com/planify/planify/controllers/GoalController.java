package com.planify.planify.controllers;

import com.planify.planify.dtos.GoalRequestDto;
import com.planify.planify.entities.Goal;
import com.planify.planify.repositories.GoalRepository;
import com.planify.planify.repositories.UserRepository;
import com.planify.planify.services.GoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("v1/goals")
public class GoalController {
    private final UserRepository userRepository;
    private final GoalService goalService;

    public GoalController(UserRepository userRepository, GoalService goalService) {
        this.userRepository = userRepository;
        this.goalService = goalService;
    }

    /*
    @PostMapping
    public ResponseEntity<Category> createCategory(Principal principal, @RequestBody CategoryRequestDto dto) {
        var user = userService.findByEmail(principal.getName()).orElseThrow();
        var id = categoryService.createCategory(user.getUserId(), dto);
        return ResponseEntity.of(id);
    }*/

    @PostMapping
    public ResponseEntity<Goal> createGoal(Principal principal, @RequestBody GoalRequestDto dto) {
        var user = userRepository.findByEmail(principal.getName()).orElseThrow();
        var goal = goalService.createGoal(user.getUserId(), dto);
        return ResponseEntity.of(goal);
    }
}
