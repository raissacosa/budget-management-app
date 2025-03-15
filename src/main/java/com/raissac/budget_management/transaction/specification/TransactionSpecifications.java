package com.raissac.budget_management.transaction.specification;

import com.raissac.budget_management.security.entity.User;
import com.raissac.budget_management.transaction.dto.TransactionFilterRequest;
import com.raissac.budget_management.transaction.entity.Transaction;
import com.raissac.budget_management.transaction.entity.TransactionType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionSpecifications {

    public static Specification<Transaction> withFilters(TransactionFilterRequest request, User user) {
        return Specification.where(
                byUser(user)
                        .and(minAmount(request.minAmount()))
                        .and(maxAmount(request.maxAmount()))
                        .and(startDate(request.startDate()))
                        .and(endDate(request.endDate()))
                        .and(type(request.type()))
                        .and(category(request.categoryId()))
        );
    }

    public static Specification<Transaction> byUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    public static Specification<Transaction> minAmount(BigDecimal minAmount) {
        return (root, query, cb) -> minAmount != null ? cb.greaterThanOrEqualTo(root.get("amount"), minAmount) : null;
    }

    public static Specification<Transaction> maxAmount(BigDecimal maxAmount) {
        return (root, query, cb) -> maxAmount != null ? cb.lessThanOrEqualTo(root.get("amount"), maxAmount) : null;
    }

    public static Specification<Transaction> category(Long categoryId){
        return (root, query, cb) -> categoryId != null ? cb.equal(root.get("category").get("id"), categoryId) : null;
    }

    public static Specification<Transaction> startDate(LocalDate startDate){
        return (root, query, cb) -> startDate != null ? cb.greaterThanOrEqualTo(root.get("date"), startDate) : null;
    }

    public static Specification<Transaction> endDate(LocalDate endDate){
        return (root, query, cb) -> endDate != null ? cb.lessThanOrEqualTo(root.get("date"), endDate) : null;
    }

    public static Specification<Transaction> type(TransactionType type){
        return (root, query, cb) -> type != null ? cb.equal(root.get("type"), type) : null;
    }

}
