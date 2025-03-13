package com.raissac.budget_management.transaction.controller;

import com.raissac.budget_management.transaction.dto.TransactionRequest;
import com.raissac.budget_management.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping(value = "/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<String> createTransaction(@RequestBody TransactionRequest transactionRequest) {
        transactionService.createTransaction(transactionRequest);
        return ResponseEntity.ok("Transaction created successfully");
    }
}
