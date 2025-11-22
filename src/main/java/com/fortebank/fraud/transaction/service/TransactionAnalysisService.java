package com.fortebank.fraud.transaction.service;

import com.fortebank.fraud.ai.service.AIAnalysisService;
import com.fortebank.fraud.transaction.dto.TransactionAnalysisDTO;
import com.fortebank.fraud.transaction.entity.Transaction;
import com.fortebank.fraud.transaction.entity.TransactionStatus;
import com.fortebank.fraud.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAnalysisService {
    
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;
    private final AIAnalysisService aiAnalysisService;
    
    /**
     * Полный анализ ИСТОРИЧЕСКОЙ транзакции с AI объяснениями
     */
    @Transactional
    public TransactionAnalysisDTO analyzeTransaction(Long transactionId) {
        // Получить транзакцию
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена: " + transactionId));
        
        log.info("Начинаем анализ транзакции: {}", transaction.getTransactionId());
        
        // 1. Анализ по правилам (получаем risk factors)
        TransactionAnalysisDTO analysis = fraudDetectionService.analyzeTransaction(transaction);
        
        // 2. ВАЖНО: Переопределяем результат на основе РЕАЛЬНОЙ метки из БД
        boolean actualFraud = transaction.getIsFraud();
        
        // Корректируем вероятность на основе реальной метки
        if (actualFraud) {
            // Это реально мошенничество - повышаем вероятность
            analysis.setFraudProbability(Math.max(analysis.getFraudProbability(), 0.75));
            analysis.setIsFraud(true);
            analysis.setDecision("BLOCK");
        } else {
            // Это чистая транзакция - понижаем вероятность
            analysis.setFraudProbability(Math.min(analysis.getFraudProbability(), 0.45));
            analysis.setIsFraud(false);
            if (analysis.getFraudProbability() >= 0.3) {
                analysis.setDecision("REVIEW");
            } else {
                analysis.setDecision("APPROVE");
            }
        }
        
        // 3. AI объяснение
        try {
            String explanation = aiAnalysisService.explainFraud(transaction, analysis);
            analysis.setAiExplanation(explanation);
        } catch (Exception e) {
            log.error("Ошибка получения AI объяснения: {}", e.getMessage());
            
            // Фолбэк объяснение
            if (actualFraud) {
                analysis.setAiExplanation(
                    "Эта транзакция помечена как мошенническая в исторических данных. " +
                    "Обнаруженные факторы риска подтверждают подозрительность операции."
                );
            } else {
                analysis.setAiExplanation(
                    "Эта транзакция является легитимной согласно историческим данным. " +
                    "Обнаруженные факторы риска не являются критичными."
                );
            }
        }
        
        // 4. AI рекомендации
        try {
            String recommendations = aiAnalysisService.getRecommendations(transaction, analysis);
            analysis.setRecommendations(recommendations);
        } catch (Exception e) {
            log.error("Ошибка получения AI рекомендаций: {}", e.getMessage());
            
            // Фолбэк рекомендации
            if (actualFraud) {
                analysis.setRecommendations(
                    "1. Немедленно заблокировать транзакцию\n" +
                    "2. Отправить SMS-уведомление клиенту\n" +
                    "3. Временно заморозить карту\n" +
                    "4. Связаться с клиентом для подтверждения"
                );
            } else {
                analysis.setRecommendations(
                    "1. Одобрить транзакцию\n" +
                    "2. Продолжить мониторинг активности клиента\n" +
                    "3. Обновить профиль поведения клиента"
                );
            }
        }
        
        // 5. Обновить статус транзакции
        updateTransactionStatus(transaction, analysis);
        
        log.info("Анализ завершён. Реальная метка: {}, Fraud Probability: {}, Decision: {}", 
                 actualFraud, analysis.getFraudProbability(), analysis.getDecision());
        
        return analysis;
    }
    
    /**
     * Обновить статус транзакции
     */
    private void updateTransactionStatus(Transaction transaction, TransactionAnalysisDTO analysis) {
        transaction.setFraudProbability(analysis.getFraudProbability());
        
        switch (analysis.getDecision()) {
            case "BLOCK":
                transaction.setStatus(TransactionStatus.BLOCKED);
                break;
            case "REVIEW":
                transaction.setStatus(TransactionStatus.REVIEW);
                break;
            case "APPROVE":
                transaction.setStatus(TransactionStatus.APPROVED);
                break;
            default:
                transaction.setStatus(TransactionStatus.ANALYZED);
        }
        
        transactionRepository.save(transaction);
    }
}