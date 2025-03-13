package com.raissac.budget_management.transaction.repository;

import com.raissac.budget_management.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
