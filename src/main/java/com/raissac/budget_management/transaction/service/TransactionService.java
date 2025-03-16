package com.raissac.budget_management.transaction.service;

import com.raissac.budget_management.exception.*;
import com.raissac.budget_management.transaction.dto.*;
import com.raissac.budget_management.category.entity.Category;
import com.raissac.budget_management.category.repository.CategoryRepository;
import com.raissac.budget_management.common.PageResponse;
import com.raissac.budget_management.security.entity.User;
import com.raissac.budget_management.security.repository.UserRepository;
import com.raissac.budget_management.transaction.entity.Transaction;
import com.raissac.budget_management.transaction.mapper.TransactionMapper;
import com.raissac.budget_management.transaction.repository.TransactionRepository;
import com.raissac.budget_management.transaction.specification.TransactionSpecifications;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.Month;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    public Transaction createTransaction(TransactionRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

        Transaction newTransaction = Transaction.builder()
                .amount(request.amount())
                .description(request.description())
                .date(request.date())
                .type(request.type())
                .category(category)
                .user(user)
                .build();

        return transactionRepository.save(newTransaction);
    }

    public PageResponse<TransactionResponse> findAllTransactions(TransactionFilterRequest request, int page, int size){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());

        Specification<Transaction> specification = TransactionSpecifications.withFilters(request, user);

        Page<Transaction> transactions = transactionRepository.findAll(specification, pageable);

        List<TransactionResponse> transactionResponses = transactions.stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();

        return new PageResponse<>(
                transactionResponses,
                transactions.getNumber(),
                transactions.getNumberOfElements(),
                transactions.getTotalPages(),
                transactions.isFirst(),
                transactions.isLast()
        );

    }

    public void deleteTransaction(Long id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction with id: " + id + "not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You are not allowed to delete this transaction");
        }
        transactionRepository.deleteById(id);

    }

    public List<TotalSpentPerCategoryResponse> getTotalSpentPerCategory(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        return transactionRepository.getTotalSpentPerCategory(email);
    }

    public BalanceResponse getAccountBalance(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        BigDecimal income = transactionRepository.getTotalIncome(email);
        BigDecimal expenses = transactionRepository.getTotalExpenses(email);
        BigDecimal balance = income.subtract(expenses);

        return new BalanceResponse(income,expenses,balance);
    }

    public List<MonthlyTransactionSummaryResponse> getMonthlySummary(int year){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        List<Object[]> rows = transactionRepository.getMonthlySummary(email, year);

        return rows.stream()
                .map(row -> new MonthlyTransactionSummaryResponse(
                        Month.of((int) row[0]),
                        (BigDecimal) row[1],
                        (BigDecimal) row[2]
                ))
                .toList();

    }

    public List<TopSpendingCategoryResponse> getTopSpendingCategories(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Pageable top3 = PageRequest.of(0,3);
        return transactionRepository.findTopSpendingCategories(email, top3);
    }

    public ByteArrayInputStream exportTransactionsToCSV(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        List<Transaction> transactions = transactionRepository.findAllByUserEmail(email);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out),
                     CSVFormat.DEFAULT.withHeader("ID", "Amount", "Date", "Description", "Type", "Category"))) {

            for (Transaction t : transactions) {
                csvPrinter.printRecord(
                        t.getId(),
                        t.getAmount(),
                        t.getDate(),
                        t.getDescription(),
                        t.getType(),
                        t.getCategory().getName()
                );
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new CsvExportException("Failed to export CSV: " + e.getMessage());
        }
    }
}
