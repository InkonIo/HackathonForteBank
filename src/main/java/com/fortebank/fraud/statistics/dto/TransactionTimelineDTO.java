package com.fortebank.fraud.statistics.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionTimelineDTO {
    private Long transactionId;
    private String transactionDate;
    private BigDecimal amount;
    private Boolean isFraud;
    private String recipientId;
    private Double riskScore;
}
