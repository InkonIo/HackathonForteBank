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
public class TimeSeriesDataPoint {
    private String date;
    private Long count;
    private BigDecimal amount;
    private Double avgRiskScore;
}
