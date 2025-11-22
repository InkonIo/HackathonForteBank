package com.fortebank.fraud.transaction.service;

import com.fortebank.fraud.customer.dto.CustomerStats;
import com.fortebank.fraud.customer.service.BehaviorAnalysisService;
import com.fortebank.fraud.customer.service.CustomerStatsService;
import com.fortebank.fraud.transaction.dto.RiskFactorDTO;
import com.fortebank.fraud.transaction.dto.TransactionAnalysisDTO;
import com.fortebank.fraud.transaction.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudDetectionService {
    
    private final CustomerStatsService customerStatsService;
    private final BehaviorAnalysisService behaviorAnalysisService;  // ← НОВОЕ!
    
    // Пороговые значения
    private static final double HIGH_AMOUNT_MULTIPLIER = 3.0;
    private static final int NIGHT_START_HOUR = 0;
    private static final int NIGHT_END_HOUR = 6;
    private static final int MAX_TRANSACTIONS_PER_HOUR = 3;
    private static final int MAX_TRANSACTIONS_PER_DAY = 15;
    private static final BigDecimal LARGE_AMOUNT_THRESHOLD = new BigDecimal("100000");
    
    /**
     * Анализировать транзакцию на мошенничество
     */
    public TransactionAnalysisDTO analyzeTransaction(Transaction transaction) {
        log.info("Анализ транзакции: {}", transaction.getTransactionId());
        
        // Получить статистику клиента
        CustomerStats stats = customerStatsService.getCustomerStats(transaction.getCustomerId());
        
        // Проверить, новый ли получатель
        boolean isNewRecipient = customerStatsService.isNewRecipient(
                transaction.getCustomerId(),
                transaction.getRecipientId()
        );
        
        // Анализ по правилам
        List<RiskFactorDTO> riskFactors = new ArrayList<>();
        int totalScore = 0;
        
        // 1. Анализ суммы
        RiskFactorDTO amountRisk = analyzeAmount(transaction.getAmount(), stats);
        if (amountRisk != null) {
            riskFactors.add(amountRisk);
            totalScore += amountRisk.getScore();
        }
        
        // 2. Анализ времени
        RiskFactorDTO timeRisk = analyzeTime(transaction.getTransactionDateTime());
        if (timeRisk != null) {
            riskFactors.add(timeRisk);
            totalScore += timeRisk.getScore();
        }
        
        // 3. Анализ получателя
        if (isNewRecipient) {
            RiskFactorDTO recipientRisk = RiskFactorDTO.builder()
                    .name("Новый получатель")
                    .description("Клиент ранее не переводил средства на этот счёт")
                    .score(25)
                    .weight(0.25)
                    .build();
            riskFactors.add(recipientRisk);
            totalScore += 25;
        }
        
        // 4. Анализ частоты транзакций
        RiskFactorDTO frequencyRisk = analyzeFrequency(stats);
        if (frequencyRisk != null) {
            riskFactors.add(frequencyRisk);
            totalScore += frequencyRisk.getScore();
        }
        
        // 5. Большая сумма
        if (transaction.getAmount().compareTo(LARGE_AMOUNT_THRESHOLD) > 0) {
            RiskFactorDTO largeAmountRisk = RiskFactorDTO.builder()
                    .name("Очень большая сумма")
                    .description(String.format("Сумма %.2f₸ превышает порог %.2f₸",
                            transaction.getAmount(), LARGE_AMOUNT_THRESHOLD))
                    .score(20)
                    .weight(0.20)
                    .build();
            riskFactors.add(largeAmountRisk);
            totalScore += 20;
        }
        
        // ✨ 6. НОВОЕ: Анализ поведенческих паттернов
        try {
            List<RiskFactorDTO> behaviorRisks = behaviorAnalysisService.analyzeBehaviorPatterns(
                    transaction.getCustomerId(),
                    transaction.getTransactionDateTime().toLocalDate()
            );
            
            riskFactors.addAll(behaviorRisks);
            totalScore += behaviorRisks.stream()
                    .mapToInt(RiskFactorDTO::getScore)
                    .sum();
                    
            log.debug("Добавлено {} поведенческих факторов риска", behaviorRisks.size());
        } catch (Exception e) {
            log.warn("Не удалось получить поведенческие факторы риска: {}", e.getMessage());
        }
        
        // Рассчитать вероятность мошенничества (0-1)
        double fraudProbability = Math.min(totalScore / 100.0, 1.0);
        
        // Определить решение
        String decision = determineDecision(fraudProbability, totalScore);
        
        return TransactionAnalysisDTO.builder()
                .transactionId(transaction.getId())
                .customerId(transaction.getCustomerId())
                .fraudProbability(fraudProbability)
                .isFraud(fraudProbability >= 0.70)
                .decision(decision)
                .riskScore(totalScore)
                .riskFactors(riskFactors)
                .analyzedAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Анализ суммы транзакции
     */
    private RiskFactorDTO analyzeAmount(BigDecimal amount, CustomerStats stats) {
        if (stats.getTotalTransactions() == 0) {
            return null;
        }
        
        BigDecimal avgAmount = stats.getAvgAmount();
        if (avgAmount.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        
        double ratio = amount.divide(avgAmount, 2, RoundingMode.HALF_UP).doubleValue();
        
        if (ratio >= HIGH_AMOUNT_MULTIPLIER) {
            int score = (int) Math.min(30 + (ratio - HIGH_AMOUNT_MULTIPLIER) * 5, 40);
            
            return RiskFactorDTO.builder()
                    .name("Аномальная сумма")
                    .description(String.format("Сумма %.2f₸ в %.1f раз больше средней (%.2f₸)",
                            amount, ratio, avgAmount))
                    .score(score)
                    .weight(score / 100.0)
                    .build();
        }
        
        return null;
    }
    
    /**
     * Анализ времени транзакции
     */
    private RiskFactorDTO analyzeTime(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        
        if (hour >= NIGHT_START_HOUR && hour < NIGHT_END_HOUR) {
            return RiskFactorDTO.builder()
                    .name("Ночное время")
                    .description(String.format("Транзакция совершена в %02d:00 (ночное время)", hour))
                    .score(20)
                    .weight(0.20)
                    .build();
        }
        
        return null;
    }
    
    /**
     * Анализ частоты транзакций
     */
    private RiskFactorDTO analyzeFrequency(CustomerStats stats) {
        if (stats.getTransactionCount1h() > MAX_TRANSACTIONS_PER_HOUR) {
            return RiskFactorDTO.builder()
                    .name("Высокая частота транзакций")
                    .description(String.format("За последний час %d транзакций (обычно до %d)",
                            stats.getTransactionCount1h(), MAX_TRANSACTIONS_PER_HOUR))
                    .score(25)
                    .weight(0.25)
                    .build();
        }
        
        if (stats.getTransactionCount24h() > MAX_TRANSACTIONS_PER_DAY) {
            return RiskFactorDTO.builder()
                    .name("Необычная активность")
                    .description(String.format("За последние 24 часа %d транзакций (обычно до %d)",
                            stats.getTransactionCount24h(), MAX_TRANSACTIONS_PER_DAY))
                    .score(15)
                    .weight(0.15)
                    .build();
        }
        
        return null;
    }
    
    /**
     * Определить решение по транзакции
     */
    private String determineDecision(double fraudProbability, int riskScore) {
        if (fraudProbability >= 0.85 || riskScore >= 85) {
            return "BLOCK";
        } else if (fraudProbability >= 0.50 || riskScore >= 50) {
            return "REVIEW";
        } else {
            return "APPROVE";
        }
    }
}