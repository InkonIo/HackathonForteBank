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
     * Полный анализ транзакции с AI объяснениями
     */
    @Transactional
    public TransactionAnalysisDTO analyzeTransaction(Long transactionId) {
        // Получить транзакцию
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Транзакция не найдена: " + transactionId));
        
        log.info("Начинаем анализ транзакции: {}", transaction.getTransactionId());
        
        // 1. Анализ по правилам
        TransactionAnalysisDTO analysis = fraudDetectionService.analyzeTransaction(transaction);
        
        // 2. AI объяснение
        try {
            String explanation = aiAnalysisService.explainFraud(transaction, analysis);
            analysis.setAiExplanation(explanation);
        } catch (Exception e) {
            log.error("Ошибка получения AI объяснения: {}", e.getMessage());
            analysis.setAiExplanation("AI объяснение недоступно");
        }
        
        // 3. AI рекомендации
        try {
            String recommendations = aiAnalysisService.getRecommendations(transaction, analysis);
            analysis.setRecommendations(recommendations);
        } catch (Exception e) {
            log.error("Ошибка получения AI рекомендаций: {}", e.getMessage());
            analysis.setRecommendations("AI рекомендации недоступны");
        }
        
        // 4. Обновить статус транзакции
        updateTransactionStatus(transaction, analysis);
        
        log.info("Анализ завершён. Fraud Probability: {}, Decision: {}", 
                 analysis.getFraudProbability(), analysis.getDecision());
        
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