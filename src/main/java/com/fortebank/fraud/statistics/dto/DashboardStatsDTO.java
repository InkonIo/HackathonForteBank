package com.fortebank.fraud.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    // Общая статистика
    private Long totalTransactions;
    private Long fraudCount;
    private Long legitimateCount;
    private Double fraudRate;
    
    // Финансовая статистика
    private BigDecimal totalAmount;
    private BigDecimal fraudAmount;
    private BigDecimal avgTransactionAmount;
    
    // Решения системы
    private Long blockedCount;
    private Long reviewCount;
    private Long approvedCount;
    
    // Топ рискованных клиентов
    private List<RiskyCustomerDTO> topRiskyCustomers;
    
    // Временные данные
    private List<TimeSeriesDataPoint> fraudTrend;
    private List<TimeSeriesDataPoint> amountTrend;
}