package com.planify.planify.controllers;

import com.planify.planify.dtos.TransactionRequestDto;
import com.planify.planify.dtos.TransactionResponseDto;
import com.planify.planify.entities.Transaction;
import com.planify.planify.services.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TransactionController {
    private TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("v1/transactions")
    public ResponseEntity<Transaction> createTransaction(@RequestBody TransactionRequestDto dto) {
        return ResponseEntity.of(transactionService.createTransaction(dto));
    }

   @GetMapping("v1/transactions")
   public  ResponseEntity<List<TransactionResponseDto>> getAll() {
        var transactions = transactionService.getAll();
        return ResponseEntity.ok(transactions.stream().map(Transaction::toResponseDto).toList());
   }

   @DeleteMapping("v1/transactions")
    public ResponseEntity<Void> deleteAll() {
        transactionService.deleteAll();
        return ResponseEntity.noContent().build();
   }
}
