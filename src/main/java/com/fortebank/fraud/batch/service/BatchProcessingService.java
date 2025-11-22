package com.fortebank.fraud.batch.service;

import com.fortebank.fraud.batch.entity.BatchJob;
import com.fortebank.fraud.batch.entity.BatchJobStatus;
import com.fortebank.fraud.batch.repository.BatchJobRepository;
import com.fortebank.fraud.customer.entity.CustomerBehaviorPattern;
import com.fortebank.fraud.customer.repository.CustomerBehaviorPatternRepository;
import com.fortebank.fraud.transaction.entity.Transaction;
import com.fortebank.fraud.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchProcessingService {
    
    private final ExcelParserService excelParser;
    private final BehaviorPatternParserService behaviorPatternParser;
    private final TransactionRepository transactionRepository;
    private final BatchJobRepository batchJobRepository;
    private final CustomerBehaviorPatternRepository behaviorPatternRepository;
    
    /**
     * Обработать Excel файл с транзакциями
     */
    @Transactional
    public BatchJob processExcelFile(MultipartFile file, String createdBy) {
        // ... (существующий код остается без изменений)
        if (!excelParser.isValidExcelFile(file)) {
            throw new IllegalArgumentException("Неверный формат файла. Ожидается .xlsx или .xls");
        }
        
        BatchJob batchJob = BatchJob.builder()
                .filename(file.getOriginalFilename())
                .status(BatchJobStatus.PROCESSING)
                .startedAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();
        
        batchJob = batchJobRepository.save(batchJob);
        
        try {
            log.info("Начинаем парсинг файла: {}", file.getOriginalFilename());
            List<Transaction> transactions = excelParser.parseExcelFile(file);
            
            batchJob.setTotalRecords(transactions.size());
            batchJobRepository.save(batchJob);
            
            int batchSize = 500;
            int processed = 0;
            int failed = 0;
            
            for (int i = 0; i < transactions.size(); i += batchSize) {
                int end = Math.min(i + batchSize, transactions.size());
                List<Transaction> batch = transactions.subList(i, end);
                
                for (Transaction transaction : batch) {
                    try {
                        transaction.setBatchId(batchJob.getId());
                        transactionRepository.save(transaction);
                        processed++;
                    } catch (Exception e) {
                        log.error("Ошибка сохранения транзакции {}: {}", 
                                 transaction.getTransactionId(), e.getMessage());
                        failed++;
                    }
                }
                
                batchJob.setProcessedRecords(processed);
                batchJob.setFailedRecords(failed);
                batchJobRepository.save(batchJob);
                
                log.info("Обработано {}/{} транзакций", processed, transactions.size());
            }
            
            batchJob.setStatus(BatchJobStatus.COMPLETED);
            batchJob.setCompletedAt(LocalDateTime.now());
            batchJobRepository.save(batchJob);
            
            log.info("Обработка файла завершена. Успешно: {}, Ошибок: {}", processed, failed);
            
            return batchJob;
            
        } catch (Exception e) {
            log.error("Ошибка обработки файла: {}", e.getMessage(), e);
            
            batchJob.setStatus(BatchJobStatus.FAILED);
            batchJob.setErrorMessage(e.getMessage());
            batchJob.setCompletedAt(LocalDateTime.now());
            batchJobRepository.save(batchJob);
            
            throw new RuntimeException("Ошибка обработки файла: " + e.getMessage(), e);
        }
    }
    
    /**
     * Обработать Excel файл с поведенческими паттернами
     */
    @Transactional
    public int processBehaviorPatternsFile(MultipartFile file) {
        log.info("Начинаем обработку поведенческих паттернов: {}", file.getOriginalFilename());
        
        // Валидация
        if (!behaviorPatternParser.isValidExcelFile(file)) {
            throw new IllegalArgumentException("Неверный формат файла. Ожидается .xlsx или .xls");
        }
        
        try {
            // Парсим Excel
            List<CustomerBehaviorPattern> patterns = behaviorPatternParser.parseExcelFile(file);
            
            // Сохраняем батчами
            int saved = 0;
            int batchSize = 500;
            
            for (int i = 0; i < patterns.size(); i += batchSize) {
                int end = Math.min(i + batchSize, patterns.size());
                List<CustomerBehaviorPattern> batch = patterns.subList(i, end);
                
                behaviorPatternRepository.saveAll(batch);
                saved += batch.size();
                
                log.info("Сохранено {}/{} паттернов", saved, patterns.size());
            }
            
            log.info("Обработка завершена. Сохранено {} поведенческих паттернов", saved);
            return saved;
            
        } catch (Exception e) {
            log.error("Ошибка обработки файла поведенческих паттернов: {}", e.getMessage(), e);
            throw new RuntimeException("Ошибка обработки файла: " + e.getMessage(), e);
        }
    }
    
    /**
     * Получить статус BatchJob
     */
    public BatchJob getBatchJobStatus(Long batchId) {
        return batchJobRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("BatchJob не найден: " + batchId));
    }
    
    /**
     * Получить все BatchJob пользователя
     */
    public List<BatchJob> getUserBatchJobs(String username) {
        return batchJobRepository.findByCreatedByOrderByCreatedAtDesc(username);
    }
}
