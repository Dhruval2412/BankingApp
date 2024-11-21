package com.example.bankapp.repository;

import com.example.bankapp.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    List<Transaction> findByAccountId(Long accountId);
    List<Transaction> findByAccountIdAndType(Long accountId, String type);
    List<Transaction> findByAccountIdAndTimestampBetween(Long accountId, LocalDate startDate, LocalDate endDate);

}
