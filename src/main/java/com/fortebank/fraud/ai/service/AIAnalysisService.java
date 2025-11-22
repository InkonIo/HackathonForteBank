package com.fortebank.fraud.ai.service;

import com.fortebank.fraud.customer.service.BehaviorAnalysisService;
import com.fortebank.fraud.transaction.dto.RiskFactorDTO;
import com.fortebank.fraud.transaction.dto.TransactionAnalysisDTO;
import com.fortebank.fraud.transaction.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIAnalysisService {
    
    private final OpenAIService openAIService;
    private final BehaviorAnalysisService behaviorAnalysisService;  // ← НОВОЕ!
    
    /**
     * Получить AI объяснение мошенничества
     */
    public String explainFraud(Transaction transaction, TransactionAnalysisDTO analysis) {
        String prompt = buildExplanationPrompt(transaction, analysis);
        return openAIService.callChatGPT(prompt);
    }
    
    /**
     * Получить AI рекомендации
     */
    public String getRecommendations(Transaction transaction, TransactionAnalysisDTO analysis) {
        String prompt = buildRecommendationPrompt(transaction, analysis);
        return openAIService.callChatGPT(prompt);
    }
    
    /**
     * Построить промпт для объяснения
     */
    private String buildExplanationPrompt(Transaction transaction, TransactionAnalysisDTO analysis) {
        String riskFactorsText = analysis.getRiskFactors().stream()
                .map(rf -> String.format("- %s: %s (вес: %.0f%%)", 
                        rf.getName(), rf.getDescription(), rf.getWeight() * 100))
                .collect(Collectors.joining("\n"));
        
        // ✨ НОВОЕ: Добавляем поведенческие данные клиента
        String behaviorSummary = behaviorAnalysisService.getBehaviorSummary(
                transaction.getCustomerId());
        
        return String.format("""
                Проанализируй эту транзакцию и объясни, почему она %s.
                
                Данные транзакции:
                - Сумма: %.2f₸
                - Время: %s
                - Клиент: %s
                - Получатель: %s
                
                Результат анализа:
                - Вероятность мошенничества: %.0f%%
                - Risk Score: %d/100
                - Решение: %s
                
                Факторы риска:
                %s
                
                %s
                
                Объясни на русском языке (2-3 предложения), учитывая как финансовое поведение, 
                так и поведенческие паттерны входа в систему (смена устройств, частота логинов).
                """,
                analysis.getIsFraud() ? "подозрительная" : "безопасная",
                transaction.getAmount(),
                transaction.getTransactionDateTime(),
                transaction.getCustomerId(),
                transaction.getRecipientId(),
                analysis.getFraudProbability() * 100,
                analysis.getRiskScore(),
                analysis.getDecision(),
                riskFactorsText.isEmpty() ? "Нет факторов риска" : riskFactorsText,
                behaviorSummary
        );
    }
    
    /**
     * Построить промпт для рекомендаций
     */
    private String buildRecommendationPrompt(Transaction transaction, TransactionAnalysisDTO analysis) {
        return String.format("""
                На основе анализа транзакции (вероятность мошенничества: %.0f%%, решение: %s)
                дай конкретные рекомендации банку.
                
                Что нужно сделать:
                1. Действие с транзакцией (заблокировать/одобрить/проверить вручную)
                2. Действия с клиентом (SMS, звонок, заморозка карты)
                3. Дальнейшие шаги
                
                Ответь кратко, 3-4 пункта на русском языке.
                """,
                analysis.getFraudProbability() * 100,
                analysis.getDecision()
        );
    }
}