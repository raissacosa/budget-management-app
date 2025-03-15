package com.raissac.budget_management.transaction.repository;

import com.raissac.budget_management.category.dto.TotalSpentPerCategoryResponse;
import com.raissac.budget_management.security.entity.User;
import com.raissac.budget_management.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    @Query("""
            SELECT new com.raissac.budget_management.category.dto.TotalSpentPerCategoryResponse(t.category.name, SUM(t.amount))
            FROM Transaction t 
            WHERE t.user.email = :email
            AND t.type = 'EXPENSE'
            GROUP BY t.category.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<TotalSpentPerCategoryResponse> getTotalSpentPerCategory(String email);
}
