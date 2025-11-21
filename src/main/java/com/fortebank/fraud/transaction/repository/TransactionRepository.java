package com.fortebank.fraud.transaction.repository;

import com.fortebank.fraud.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionId(String transactionId);
    
    List<Transaction> findByCustomerId(String customerId);
    
    List<Transaction> findByBatchId(Long batchId);
    
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId " +
           "AND t.transactionDateTime >= :startDate ORDER BY t.transactionDateTime DESC")
    List<Transaction> findRecentByCustomerId(
        @Param("customerId") String customerId,
        @Param("startDate") LocalDateTime startDate
    );
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isFraud = true")
    Long countFraudulent();
    
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.isFraud = false")
    Long countLegitimate();
}