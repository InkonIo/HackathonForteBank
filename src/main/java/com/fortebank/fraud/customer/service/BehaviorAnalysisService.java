package com.fortebank.fraud.customer.service;

import com.fortebank.fraud.customer.entity.CustomerBehaviorPattern;
import com.fortebank.fraud.customer.repository.CustomerBehaviorPatternRepository;
import com.fortebank.fraud.transaction.dto.RiskFactorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BehaviorAnalysisService {
    
    private final CustomerBehaviorPatternRepository behaviorPatternRepository;
    
    // Пороговые значения для аномалий
    private static final int MAX_DEVICE_CHANGES_30D = 3;  // Макс 3 смены устройства
    private static final double HIGH_LOGIN_FREQ_CHANGE = 0.5;  // 50% рост частоты логинов
    private static final double HIGH_BURSTINESS = 0.3;  // Высокая взрывность
    private static final double HIGH_ZSCORE = 2.0;  // Z-score > 2 = аномалия
    
    /**
     * Анализировать поведенческие паттерны клиента
     */
    public List<RiskFactorDTO> analyzeBehaviorPatterns(String customerId, LocalDate transactionDate) {
        List<RiskFactorDTO> riskFactors = new ArrayList<>();
        
        // Получаем последний паттерн клиента
        Optional<CustomerBehaviorPattern> patternOpt = 
                behaviorPatternRepository.findLatestByCustomerId(customerId);
        
        if (patternOpt.isEmpty()) {
            log.debug("Нет поведенческих данных для клиента {}", customerId);
            return riskFactors;
        }
        
        CustomerBehaviorPattern pattern = patternOpt.get();
        
        // 1. Анализ смены устройств
        analyzeDeviceChanges(pattern, riskFactors);
        
        // 2. Анализ частоты логинов
        analyzeLoginFrequency(pattern, riskFactors);
        
        // 3. Анализ взрывности активности
        analyzeBurstiness(pattern, riskFactors);
        
        // 4. Анализ интервалов между сессиями
        analyzeSessionIntervals(pattern, riskFactors);
        
        return riskFactors;
    }
    
    /**
     * 1. Анализ смены устройств
     */
    private void analyzeDeviceChanges(CustomerBehaviorPattern pattern, List<RiskFactorDTO> riskFactors) {
        int uniqueOsVersions = pattern.getUniqueOsVersions30d();
        int uniquePhoneModels = pattern.getUniquePhoneModels30d();
        
        // Если клиент часто меняет устройства - подозрительно
        if (uniqueOsVersions > MAX_DEVICE_CHANGES_30D || uniquePhoneModels > MAX_DEVICE_CHANGES_30D) {
            int score = Math.min(25 + (uniquePhoneModels * 5), 35);
            
            riskFactors.add(RiskFactorDTO.builder()
                    .name("Частая смена устройств")
                    .description(String.format(
                            "За 30 дней использовано %d разных устройств и %d версий ОС",
                            uniquePhoneModels, uniqueOsVersions))
                    .score(score)
                    .weight(score / 100.0)
                    .build());
        }
    }
    
    /**
     * 2. Анализ частоты логинов
     */
    private void analyzeLoginFrequency(CustomerBehaviorPattern pattern, List<RiskFactorDTO> riskFactors) {
        BigDecimal loginFreqChange = pattern.getLoginFreqChangeRatio();
        
        if (loginFreqChange != null && 
            loginFreqChange.doubleValue() > HIGH_LOGIN_FREQ_CHANGE) {
            
            int score = 20;
            
            riskFactors.add(RiskFactorDTO.builder()
                    .name("Резкий рост активности")
                    .description(String.format(
                            "Частота логинов выросла на %.0f%% за последние 7 дней",
                            loginFreqChange.doubleValue() * 100))
                    .score(score)
                    .weight(0.20)
                    .build());
        }
    }
    
    /**
     * 3. Анализ взрывности активности (burstiness)
     */
    private void analyzeBurstiness(CustomerBehaviorPattern pattern, List<RiskFactorDTO> riskFactors) {
        BigDecimal burstiness = pattern.getBurstinessScore();
        
        if (burstiness != null && burstiness.doubleValue() > HIGH_BURSTINESS) {
            int score = 15;
            
            riskFactors.add(RiskFactorDTO.builder()
                    .name("Неравномерная активность")
                    .description(String.format(
                            "Обнаружена 'взрывная' активность (score: %.2f) - резкие всплески логинов",
                            burstiness))
                    .score(score)
                    .weight(0.15)
                    .build());
        }
    }
    
    /**
     * 4. Анализ интервалов между сессиями
     */
    private void analyzeSessionIntervals(CustomerBehaviorPattern pattern, List<RiskFactorDTO> riskFactors) {
        BigDecimal zScore = pattern.getIntervalZscore();
        
        if (zScore != null && Math.abs(zScore.doubleValue()) > HIGH_ZSCORE) {
            int score = 15;
            
            riskFactors.add(RiskFactorDTO.builder()
                    .name("Аномальные интервалы входа")
                    .description(String.format(
                            "Интервалы между сессиями аномально отличаются (Z-score: %.2f)",
                            zScore))
                    .score(score)
                    .weight(0.15)
                    .build());
        }
    }
    
    /**
     * Получить краткую сводку поведения клиента для AI
     */
    public String getBehaviorSummary(String customerId) {
        Optional<CustomerBehaviorPattern> patternOpt = 
                behaviorPatternRepository.findLatestByCustomerId(customerId);
        
        if (patternOpt.isEmpty()) {
            return "Нет данных о поведении клиента";
        }
        
        CustomerBehaviorPattern p = patternOpt.get();
        
        return String.format("""
                Поведенческие данные клиента:
                - Устройства за 30 дней: %d моделей телефона, %d версий ОС
                - Активность: %d логинов за 7 дней, %d за 30 дней
                - Изменение частоты логинов: %.0f%%
                - Показатель взрывности: %.2f
                - Z-score интервалов: %.2f
                """,
                p.getUniquePhoneModels30d(),
                p.getUniqueOsVersions30d(),
                p.getLoginsLast7Days(),
                p.getLoginsLast30Days(),
                (p.getLoginFreqChangeRatio() != null ? p.getLoginFreqChangeRatio().doubleValue() * 100 : 0),
                (p.getBurstinessScore() != null ? p.getBurstinessScore().doubleValue() : 0),
                (p.getIntervalZscore() != null ? p.getIntervalZscore().doubleValue() : 0)
        );
    }
}