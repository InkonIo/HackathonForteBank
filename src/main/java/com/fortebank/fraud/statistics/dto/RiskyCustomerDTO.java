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
public class RiskyCustomerDTO {
    private String customerId;
    private Long transactionCount;
    private Long fraudCount;
    private Double fraudRate;
    private BigDecimal totalAmount;
    private Double avgRiskScore;
}
