package com.fortebank.fraud.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStats {
    private String customerId;
    private Integer totalTransactions;
    private BigDecimal avgAmount;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Integer transactionCount1h;
    private Integer transactionCount24h;
    private LocalDateTime lastTransactionDate;
    private Integer uniqueRecipients;
}