package com.planify.planify.controllers;

import com.planify.planify.dtos.user.UserRequestDto;
import com.planify.planify.entities.User;
import com.planify.planify.security.JwtService;
import com.planify.planify.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRequestDto userDto) {
        if (userService.registerUser(userDto)) {
            return ResponseEntity.ok("User registered successfully!");
        } else {
            return ResponseEntity.badRequest().body("User was not registered!");
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserRequestDto userDto) {
        if (userDto.email() == null || userDto.password() == null) {
            return ResponseEntity.badRequest().build();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.email(), userDto.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Obtém o usuário autenticado
        User user = userService.findByEmail(userDto.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Gera o token JWT incluindo o ID do usuário
        String token = jwtService.generateToken(user.getUserId(), authentication.getName());

        // Retorna o token na resposta
        return ResponseEntity.ok(token);
    }
}
