package com.fortebank.fraud.statistics.service;

import com.fortebank.fraud.customer.entity.CustomerBehaviorPattern;
import com.fortebank.fraud.customer.repository.CustomerBehaviorPatternRepository;
import com.fortebank.fraud.statistics.dto.*;
import com.fortebank.fraud.transaction.entity.Transaction;
import com.fortebank.fraud.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {
    
    private final TransactionRepository transactionRepository;
    private final CustomerBehaviorPatternRepository behaviorPatternRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Получить статистику для дашборда
     */
    public DashboardStatsDTO getDashboardStats() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        
        // Общая статистика
        long totalTransactions = allTransactions.size();
        long fraudCount = allTransactions.stream().filter(Transaction::getIsFraud).count();
        long legitimateCount = totalTransactions - fraudCount;
        double fraudRate = totalTransactions > 0 ? (fraudCount * 100.0 / totalTransactions) : 0;
        
        // Финансовая статистика
        BigDecimal totalAmount = allTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal fraudAmount = allTransactions.stream()
                .filter(Transaction::getIsFraud)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgAmount = totalTransactions > 0 
                ? totalAmount.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Решения системы
        long blockedCount = allTransactions.stream()
                .filter(t -> t.getFraudProbability() != null && t.getFraudProbability() >= 0.85)
                .count();
        
        long reviewCount = allTransactions.stream()
                .filter(t -> t.getFraudProbability() != null && 
                            t.getFraudProbability() >= 0.50 && 
                            t.getFraudProbability() < 0.85)
                .count();
        
        long approvedCount = totalTransactions - blockedCount - reviewCount;
        
        // Топ рискованных клиентов
        List<RiskyCustomerDTO> topRiskyCustomers = getTopRiskyCustomers(allTransactions);
        
        // Временные данные
        List<TimeSeriesDataPoint> fraudTrend = getFraudTrend(allTransactions);
        List<TimeSeriesDataPoint> amountTrend = getAmountTrend(allTransactions);
        
        return DashboardStatsDTO.builder()
                .totalTransactions(totalTransactions)
                .fraudCount(fraudCount)
                .legitimateCount(legitimateCount)
                .fraudRate(fraudRate)
                .totalAmount(totalAmount)
                .fraudAmount(fraudAmount)
                .avgTransactionAmount(avgAmount)
                .blockedCount(blockedCount)
                .reviewCount(reviewCount)
                .approvedCount(approvedCount)
                .topRiskyCustomers(topRiskyCustomers)
                .fraudTrend(fraudTrend)
                .amountTrend(amountTrend)
                .build();
    }
    
    /**
     * Топ рискованных клиентов
     */
    private List<RiskyCustomerDTO> getTopRiskyCustomers(List<Transaction> allTransactions) {
        Map<String, List<Transaction>> byCustomer = allTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCustomerId));
        
        return byCustomer.entrySet().stream()
                .map(entry -> {
                    String customerId = entry.getKey();
                    List<Transaction> transactions = entry.getValue();
                    
                    long fraudCount = transactions.stream()
                            .filter(Transaction::getIsFraud)
                            .count();
                    
                    double fraudRate = transactions.size() > 0 
                            ? (fraudCount * 100.0 / transactions.size()) 
                            : 0;
                    
                    BigDecimal totalAmount = transactions.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    double avgRiskScore = transactions.stream()
                            .filter(t -> t.getFraudProbability() != null)
                            .mapToDouble(t -> t.getFraudProbability() * 100)
                            .average()
                            .orElse(0);
                    
                    return RiskyCustomerDTO.builder()
                            .customerId(customerId)
                            .transactionCount((long) transactions.size())
                            .fraudCount(fraudCount)
                            .fraudRate(fraudRate)
                            .totalAmount(totalAmount)
                            .avgRiskScore(avgRiskScore)
                            .build();
                })
                .filter(c -> c.getFraudCount() > 0)
                .sorted(Comparator.comparing(RiskyCustomerDTO::getFraudRate).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }
    
    /**
     * Тренд мошенничества по дням
     */
    private List<TimeSeriesDataPoint> getFraudTrend(List<Transaction> allTransactions) {
        Map<LocalDate, List<Transaction>> byDate = allTransactions.stream()
                .collect(Collectors.groupingBy(t -> t.getTransactionDateTime().toLocalDate()));
        
        return byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Transaction> transactions = entry.getValue();
                    
                    long fraudCount = transactions.stream()
                            .filter(Transaction::getIsFraud)
                            .count();
                    
                    return TimeSeriesDataPoint.builder()
                            .date(date.format(DATE_FORMATTER))
                            .count(fraudCount)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Тренд сумм по дням
     */
    private List<TimeSeriesDataPoint> getAmountTrend(List<Transaction> allTransactions) {
        Map<LocalDate, List<Transaction>> byDate = allTransactions.stream()
                .collect(Collectors.groupingBy(t -> t.getTransactionDateTime().toLocalDate()));
        
        return byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Transaction> transactions = entry.getValue();
                    
                    BigDecimal totalAmount = transactions.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    return TimeSeriesDataPoint.builder()
                            .date(date.format(DATE_FORMATTER))
                            .amount(totalAmount)
                            .count((long) transactions.size())
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Аналитика клиента
     */
    public CustomerAnalyticsDTO getCustomerAnalytics(String customerId) {
        List<Transaction> transactions = transactionRepository.findByCustomerId(customerId);
        
        // Транзакционная активность
        long totalTransactions = transactions.size();
        long fraudTransactions = transactions.stream()
                .filter(Transaction::getIsFraud)
                .count();
        
        BigDecimal totalAmount = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgAmount = totalTransactions > 0
                ? totalAmount.divide(BigDecimal.valueOf(totalTransactions), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        // Поведенческие данные
        Optional<CustomerBehaviorPattern> behaviorOpt = 
                behaviorPatternRepository.findLatestByCustomerId(customerId);
        
        Integer deviceChanges = behaviorOpt.map(CustomerBehaviorPattern::getUniquePhoneModels30d).orElse(0);
        Integer osVersionChanges = behaviorOpt.map(CustomerBehaviorPattern::getUniqueOsVersions30d).orElse(0);
        Integer loginsLast7Days = behaviorOpt.map(CustomerBehaviorPattern::getLoginsLast7Days).orElse(0);
        Integer loginsLast30Days = behaviorOpt.map(CustomerBehaviorPattern::getLoginsLast30Days).orElse(0);
        Double loginFrequencyChange = behaviorOpt
                .map(CustomerBehaviorPattern::getLoginFreqChangeRatio)
                .map(BigDecimal::doubleValue)
                .orElse(0.0);
        
        // Временная линия транзакций
        List<TransactionTimelineDTO> timeline = transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionDateTime))
                .map(t -> TransactionTimelineDTO.builder()
                        .transactionId(t.getId())
                        .transactionDate(t.getTransactionDateTime().format(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .amount(t.getAmount())
                        .isFraud(t.getIsFraud())
                        .recipientId(t.getRecipientId())
                        .riskScore(t.getFraudProbability() != null ? t.getFraudProbability() * 100 : 0)
                        .build())
                .collect(Collectors.toList());
        
        // График сумм по дням
        Map<LocalDate, List<Transaction>> byDate = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getTransactionDateTime().toLocalDate()));
        
        List<AmountTimeSeriesDTO> amountTimeline = byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<Transaction> dayTransactions = entry.getValue();
                    
                    BigDecimal dayAmount = dayTransactions.stream()
                            .map(Transaction::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    boolean hasFraud = dayTransactions.stream()
                            .anyMatch(Transaction::getIsFraud);
                    
                    return AmountTimeSeriesDTO.builder()
                            .date(date.format(DATE_FORMATTER))
                            .amount(dayAmount)
                            .isFraud(hasFraud)
                            .transactionCount(dayTransactions.size())
                            .build();
                })
                .collect(Collectors.toList());
        
        return CustomerAnalyticsDTO.builder()
                .customerId(customerId)
                .totalTransactions(totalTransactions)
                .fraudTransactions(fraudTransactions)
                .totalAmount(totalAmount)
                .avgAmount(avgAmount)
                .deviceChanges(deviceChanges)
                .osVersionChanges(osVersionChanges)
                .loginsLast7Days(loginsLast7Days)
                .loginsLast30Days(loginsLast30Days)
                .loginFrequencyChange(loginFrequencyChange)
                .transactionTimeline(timeline)
                .amountTimeline(amountTimeline)
                .build();
    }
}