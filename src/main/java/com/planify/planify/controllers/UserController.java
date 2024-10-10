package com.planify.planify.controllers;

import com.planify.planify.dtos.UserRequestDto;
import com.planify.planify.dtos.UserResponseDto;
import com.planify.planify.entities.User;
import com.planify.planify.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getLoggedUser(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.of(userService.findByEmail(email).map(User::toResponseDto));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateLoggedUser(Principal principal, @RequestBody UserRequestDto dto) {
        User user = userService.findByEmail(principal.getName()).orElseThrow();
        return ResponseEntity.of(userService.updateById(user.getUserId(), dto).map(User::toResponseDto));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteLoggedUser(Principal principal) {
        User user = userService.findByEmail(principal.getName()).orElseThrow();
        userService.deleteById(user.getUserId());
        return ResponseEntity.noContent().build();
    }

}
