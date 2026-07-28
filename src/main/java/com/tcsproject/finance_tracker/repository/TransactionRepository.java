package com.tcsproject.finance_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcsproject.finance_tracker.entity.*;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
}
