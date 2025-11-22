package com.fortebank.fraud.batch.controller;

import com.fortebank.fraud.batch.entity.BatchJob;
import com.fortebank.fraud.batch.service.BatchProcessingService;
import com.fortebank.fraud.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/batch" )
@RequiredArgsConstructor
@Slf4j
public class BatchController {
    
    private final BatchProcessingService batchProcessingService;
    
    /**
     * Загрузить Excel файл с транзакциями
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<BatchJob>> uploadTransactions(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        log.info("Получен файл для загрузки: {}", file.getOriginalFilename());
        
        String username = authentication.getName();
        BatchJob batchJob = batchProcessingService.processExcelFile(file, username);
        
        return ResponseEntity.ok(ApiResponse.success(
            batchJob,
            "Файл успешно загружен и обработан"
        ));
    }
    
    /**
     * Получить статус обработки
     */
    @GetMapping("/status/{batchId}")
    public ResponseEntity<ApiResponse<BatchJob>> getBatchStatus(
            @PathVariable Long batchId) {
        
        BatchJob batchJob = batchProcessingService.getBatchJobStatus(batchId);
        
        return ResponseEntity.ok(ApiResponse.success(
            batchJob,
            "Статус загружен"
        ));
    }
    
    /**
     * Получить историю загрузок
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<BatchJob>>> getBatchHistory(
            Authentication authentication) {
        
        String username = authentication.getName();
        List<BatchJob> history = batchProcessingService.getUserBatchJobs(username);
        
        return ResponseEntity.ok(ApiResponse.success(
            history,
            "История загружена"
        ));
    }

    @PostMapping("/upload-behavior")
    public ResponseEntity<ApiResponse<String>> uploadBehaviorPatterns(
            @RequestParam("file") MultipartFile file) {
        
        log.info("Загрузка поведенческих паттернов: {}", file.getOriginalFilename());
        
        int saved = batchProcessingService.processBehaviorPatternsFile(file);
        
        return ResponseEntity.ok(ApiResponse.success(
            "Загружено " + saved + " поведенческих паттернов",
            "Паттерны успешно загружены"
        ));
    }
}
