package com.fortebank.fraud.statistics.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalyticsDTO {
    private String customerId;
    
    // Транзакционная активность
    private Long totalTransactions;
    private Long fraudTransactions;
    private BigDecimal totalAmount;
    private BigDecimal avgAmount;
    
    // Поведенческие данные
    private Integer deviceChanges;
    private Integer osVersionChanges;
    private Integer loginsLast7Days;
    private Integer loginsLast30Days;
    private Double loginFrequencyChange;
    
    // Временная линия транзакций
    private List<TransactionTimelineDTO> transactionTimeline;
    
    // График сумм по времени
    private List<AmountTimeSeriesDTO> amountTimeline;
    
    // Устройства
    private List<DeviceUsageDTO> deviceUsage;
}
