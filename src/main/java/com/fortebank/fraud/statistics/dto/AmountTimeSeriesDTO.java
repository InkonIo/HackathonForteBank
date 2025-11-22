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
public class AmountTimeSeriesDTO {
    private String date;
    private BigDecimal amount;
    private Boolean isFraud;
    private Integer transactionCount;
}