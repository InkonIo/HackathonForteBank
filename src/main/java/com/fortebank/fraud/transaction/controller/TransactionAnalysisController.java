package com.fortebank.fraud.transaction.controller;

import com.fortebank.fraud.common.response.ApiResponse;
import com.fortebank.fraud.transaction.dto.TransactionAnalysisDTO;
import com.fortebank.fraud.transaction.entity.Transaction;
import com.fortebank.fraud.transaction.repository.TransactionRepository;
import com.fortebank.fraud.transaction.service.TransactionAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionAnalysisController {
    
    private final TransactionAnalysisService analysisService;
    private final TransactionRepository transactionRepository;
    
    /**
     * Получить список всех транзакций
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        
        log.info("Запрос списка транзакций: page={}, size={}", page, size);
        
        List<Transaction> transactions = transactionRepository.findAll(
            PageRequest.of(page, size, Sort.by("transactionDateTime").descending())
        ).getContent();
        
        return ResponseEntity.ok(ApiResponse.success(
            transactions,
            "Транзакции загружены"
        ));
    }
    
    /**
     * Получить только мошеннические транзакции
     */
    @GetMapping("/fraudulent")
    public ResponseEntity<ApiResponse<List<Transaction>>> getFraudulentTransactions() {
        log.info("Запрос мошеннических транзакций");
        
        List<Transaction> transactions = transactionRepository.findAll().stream()
            .filter(Transaction::getIsFraud)
            .limit(100)
            .toList();
        
        return ResponseEntity.ok(ApiResponse.success(
            transactions,
            "Мошеннические транзакции загружены"
        ));
    }
    
    /**
     * Получить транзакции клиента
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Transaction>>> getCustomerTransactions(
            @PathVariable String customerId) {
        
        log.info("Запрос транзакций клиента: {}", customerId);
        
        List<Transaction> transactions = transactionRepository.findByCustomerId(customerId);
        
        return ResponseEntity.ok(ApiResponse.success(
            transactions,
            "Транзакции клиента загружены"
        ));
    }
    
    /**
     * Анализировать транзакцию
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<ApiResponse<TransactionAnalysisDTO>> analyzeTransaction(
            @PathVariable Long id) {
        
        log.info("Запрос на анализ транзакции: {}", id);
        
        TransactionAnalysisDTO analysis = analysisService.analyzeTransaction(id);
        
        return ResponseEntity.ok(ApiResponse.success(
                analysis,
                "Анализ завершён"
        ));
    }
}