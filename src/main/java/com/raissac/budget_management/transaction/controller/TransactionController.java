package com.raissac.budget_management.transaction.controller;

import com.raissac.budget_management.common.PageResponse;
import com.raissac.budget_management.transaction.dto.TransactionFilterRequest;
import com.raissac.budget_management.transaction.dto.TransactionRequest;
import com.raissac.budget_management.transaction.dto.TransactionResponse;
import com.raissac.budget_management.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<PageResponse<TransactionResponse>> getAllTransactions(
            @ModelAttribute TransactionFilterRequest request,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity.ok(transactionService.findAllTransactions(request, page, size));
    }

    @DeleteMapping({"/{id}"})
    public ResponseEntity<String> deleteTransaction(@PathVariable Long id){
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok("Transaction deleted successfully");
    }
}
