package com.fortebank.fraud.statistics.controller;

import com.fortebank.fraud.common.response.ApiResponse;
import com.fortebank.fraud.statistics.dto.CustomerAnalyticsDTO;
import com.fortebank.fraud.statistics.dto.DashboardStatsDTO;
import com.fortebank.fraud.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j
public class StatisticsController {
    
    private final StatisticsService statisticsService;
    
    /**
     * Получить статистику для дашборда
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        log.info("Запрос статистики дашборда");
        
        DashboardStatsDTO stats = statisticsService.getDashboardStats();
        
        return ResponseEntity.ok(ApiResponse.success(
                stats,
                "Статистика загружена"
        ));
    }
    
    /**
     * Получить аналитику клиента
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<CustomerAnalyticsDTO>> getCustomerAnalytics(
            @PathVariable String customerId) {
        
        log.info("Запрос аналитики клиента: {}", customerId);
        
        CustomerAnalyticsDTO analytics = statisticsService.getCustomerAnalytics(customerId);
        
        return ResponseEntity.ok(ApiResponse.success(
                analytics,
                "Аналитика клиента загружена"
        ));
    }
}