package com.planify.planify.controllers;

import com.planify.planify.dtos.TransactionResponseDto;
import com.planify.planify.dtos.UserRequestDto;
import com.planify.planify.dtos.UserResponseDto;
import com.planify.planify.entities.Category;
import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.User;
import com.planify.planify.security.JwtService;
import com.planify.planify.services.UserService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.security.Principal;
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


    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserRequestDto dto) {
        var id = userService.createUser(dto);
        return ResponseEntity.created(URI.create("v1/users/" + id.toString())).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable("id") UUID id) {
        return ResponseEntity.of(userService.getById(id).map(User::toResponseDto));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAll() {
        var users = userService.getAll();
        return ResponseEntity.ok(users.stream().map(User::toResponseDto).toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateById(@PathVariable("id") UUID id, @RequestBody UserRequestDto dto) {
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

    @GetMapping("/export-transactions")
    public ResponseEntity<ByteArrayResource> exportCsv(Principal principal) {
        User user = userService.findByEmail(principal.getName()).orElseThrow();
        var transactions = user.getTransactions().stream().map(Transaction::toResponseDto).toList();

        // Gerar o CSV
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        writer.println("date,sender,recipient,value,is_expense,category");
        for (TransactionResponseDto t : transactions) {
            writer.printf("%s,%s,%s,\"%.2f\",%s,%s\n", t.date(), t.sender(), t.recipient(), t.value(), t.isExpense(), t.category().name());
        }
        writer.flush();
        ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=transactions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
