package com.planify.planify.controllers;

import com.planify.planify.dtos.TransactionRequestDto;
import com.planify.planify.dtos.TransactionResponseDto;
import com.planify.planify.entities.Transaction;
import com.planify.planify.services.TransactionService;
import com.planify.planify.services.UserService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
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
        var transactions = transactionService.getByUser(user);
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

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        transactionService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportCsv(Principal principal,
                                                       @RequestParam(required = false) LocalDate startDate,
                                                       @RequestParam(required = false) LocalDate endDate) {
        ByteArrayResource resource = transactionService.exportCsv(principal, startDate, endDate);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=transactions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
