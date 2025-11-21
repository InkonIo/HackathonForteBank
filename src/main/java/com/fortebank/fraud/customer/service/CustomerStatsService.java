package com.fortebank.fraud.customer.service;

import com.fortebank.fraud.customer.dto.CustomerStats;
import com.fortebank.fraud.transaction.entity.Transaction;
import com.fortebank.fraud.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerStatsService {
    
    private final TransactionRepository transactionRepository;
    
    /**
     * Получить статистику клиента
     */
    public CustomerStats getCustomerStats(String customerId) {
        List<Transaction> allTransactions = transactionRepository.findByCustomerId(customerId);
        
        if (allTransactions.isEmpty()) {
            return CustomerStats.builder()
                    .customerId(customerId)
                    .totalTransactions(0)
                    .avgAmount(BigDecimal.ZERO)
                    .minAmount(BigDecimal.ZERO)
                    .maxAmount(BigDecimal.ZERO)
                    .transactionCount1h(0)
                    .transactionCount24h(0)
                    .uniqueRecipients(0)
                    .build();
        }
        
        // Средняя сумма
        BigDecimal totalAmount = allTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgAmount = totalAmount.divide(
                BigDecimal.valueOf(allTransactions.size()),
                2,
                RoundingMode.HALF_UP
        );
        
        // Минимум и максимум
        BigDecimal minAmount = allTransactions.stream()
                .map(Transaction::getAmount)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal maxAmount = allTransactions.stream()
                .map(Transaction::getAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        // Транзакции за последний час
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long count1h = allTransactions.stream()
                .filter(t -> t.getTransactionDateTime().isAfter(oneHourAgo))
                .count();
        
        // Транзакции за последние 24 часа
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        long count24h = allTransactions.stream()
                .filter(t -> t.getTransactionDateTime().isAfter(oneDayAgo))
                .count();
        
        // Уникальные получатели
        Set<String> uniqueRecipients = allTransactions.stream()
                .map(Transaction::getRecipientId)
                .collect(Collectors.toSet());
        
        return CustomerStats.builder()
                .customerId(customerId)
                .totalTransactions(allTransactions.size())
                .avgAmount(avgAmount)
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .transactionCount1h((int) count1h)
                .transactionCount24h((int) count24h)
                .lastTransactionDate(allTransactions.get(0).getTransactionDateTime())
                .uniqueRecipients(uniqueRecipients.size())
                .build();
    }
    
    /**
     * Проверить, новый ли получатель
     */
    public boolean isNewRecipient(String customerId, String recipientId) {
        List<Transaction> customerTransactions = transactionRepository.findByCustomerId(customerId);
        
        return customerTransactions.stream()
                .noneMatch(t -> t.getRecipientId().equals(recipientId));
    }
}