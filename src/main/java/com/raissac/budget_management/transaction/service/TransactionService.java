package com.raissac.budget_management.transaction.service;

import com.raissac.budget_management.category.entity.Category;
import com.raissac.budget_management.category.repository.CategoryRepository;
import com.raissac.budget_management.common.PageResponse;
import com.raissac.budget_management.exception.AccessDeniedException;
import com.raissac.budget_management.exception.CategoryNotFoundException;
import com.raissac.budget_management.exception.TransactionNotFoundException;
import com.raissac.budget_management.exception.UserNotFoundException;
import com.raissac.budget_management.security.entity.User;
import com.raissac.budget_management.security.repository.UserRepository;
import com.raissac.budget_management.transaction.dto.TransactionFilterRequest;
import com.raissac.budget_management.transaction.dto.TransactionRequest;
import com.raissac.budget_management.transaction.dto.TransactionResponse;
import com.raissac.budget_management.transaction.entity.Transaction;
import com.raissac.budget_management.transaction.mapper.TransactionMapper;
import com.raissac.budget_management.transaction.repository.TransactionRepository;
import com.raissac.budget_management.transaction.specification.TransactionSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

}
