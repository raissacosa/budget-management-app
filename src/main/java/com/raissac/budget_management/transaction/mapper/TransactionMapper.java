package com.raissac.budget_management.transaction.mapper;

import com.raissac.budget_management.transaction.dto.TransactionResponse;
import com.raissac.budget_management.transaction.entity.Transaction;
import org.springframework.stereotype.Service;

@Service
public class TransactionMapper{

    public TransactionResponse toTransactionResponse(Transaction transaction){
        return  new TransactionResponse(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getDescription(),
                transaction.getDate(),
                transaction.getType(),
                transaction.getCategory().getName()
        );
    }

}
