package com.raissac.budget_management.transaction.controller;

import com.raissac.budget_management.transaction.dto.*;
import com.raissac.budget_management.common.PageResponse;
import com.raissac.budget_management.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController()
@RequestMapping(value = "/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<String> createTransaction(@Valid @RequestBody TransactionRequest transactionRequest) {
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

    @GetMapping("/expenses/by-category")
    public ResponseEntity<List<TotalSpentPerCategoryResponse>> getSpentByCategory()
    {
        return ResponseEntity.ok(transactionService.getTotalSpentPerCategory());
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getAccountBalance(){
        return ResponseEntity.ok(transactionService.getAccountBalance());
    }

    @GetMapping("/summary/monthly")
    public ResponseEntity<List<MonthlyTransactionSummaryResponse>> getMonthlySummary(@RequestParam int year){
        return ResponseEntity.ok(transactionService.getMonthlySummary(year));
    }

    @GetMapping("/expenses/top-categories")
    public ResponseEntity<List<TopSpendingCategoryResponse>> getTopSpendingCategories(){
        return ResponseEntity.ok(transactionService.getTopSpendingCategories());
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportCSV(){
        ByteArrayInputStream csv = transactionService.exportTransactionsToCSV();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(csv));
    }


}
