package com.planify.planify.controllers;

import com.planify.planify.dtos.UserDto;
import com.planify.planify.entities.Category;
import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.User;
import com.planify.planify.security.JwtService;
import com.planify.planify.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("v1/users")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDto userDto) {
        userService.registerUser(userDto);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserDto userDto) {
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


    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDto dto) {
        var id = userService.createUser(dto);
        return ResponseEntity.created(URI.create("v1/users/" + id.toString())).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.of(userService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateById(@PathVariable("id") UUID id, @RequestBody UserDto dto) {
        return ResponseEntity.of(userService.updateById(id, dto));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable("id") UUID id) {
        return ResponseEntity.of(userService.getTransactionsByUserId(id));
    }

    @GetMapping("/{id}/categories")
    public ResponseEntity<List<Category>> getCategories(@PathVariable("id") UUID id) {
        return ResponseEntity.of(userService.getCategoriesByUserId(id));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        userService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
