package com.planify.planify.controllers;

import com.planify.planify.dtos.transaction.TransactionRequestDto;
import com.planify.planify.dtos.transaction.TransactionResponseDto;
import com.planify.planify.entities.Transaction;
import com.planify.planify.services.TransactionService;
import com.planify.planify.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    private final UserService userService;

    public TransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(Principal principal, @RequestBody TransactionRequestDto dto) {
        var user = userService.findByEmail(principal.getName()).orElseThrow();
        return ResponseEntity.of(transactionService.createTransaction(user.getUserId(), dto));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> getAll(Principal principal) {
        var user = userService.findByEmail(principal.getName()).orElseThrow();
        var transactions = transactionService.findByUserOrderByDate(user);
        return ResponseEntity.ok(transactions.stream().map(Transaction::toResponseDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getById(Principal principal, @PathVariable("id") UUID id) {
        return ResponseEntity.of(transactionService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(Principal principal, @PathVariable("id") UUID id) {
        if (transactionService.deleteById(id)) {
            return ResponseEntity.ok("Transaction was deleted.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateById(Principal principal, @PathVariable("id") UUID id, @RequestBody TransactionRequestDto dto) {
        return ResponseEntity.of(transactionService.updateById(id, dto));
    }

}
