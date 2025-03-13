package com.raissac.budget_management.transaction.service;

import com.raissac.budget_management.category.entity.Category;
import com.raissac.budget_management.category.repository.CategoryRepository;
import com.raissac.budget_management.exception.CategoryNotFoundException;
import com.raissac.budget_management.security.entity.User;
import com.raissac.budget_management.security.repository.UserRepository;
import com.raissac.budget_management.transaction.dto.TransactionRequest;
import com.raissac.budget_management.transaction.entity.Transaction;
import com.raissac.budget_management.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public Transaction createTransaction(TransactionRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

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

}
