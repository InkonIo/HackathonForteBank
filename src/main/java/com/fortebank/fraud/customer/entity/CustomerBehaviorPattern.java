package com.fortebank.fraud.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "customer_behavior_patterns", indexes = {
    @Index(name = "idx_behavior_customer_id", columnList = "customer_id"),
    @Index(name = "idx_behavior_trans_date", columnList = "trans_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBehaviorPattern {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "trans_date", nullable = false)
    private LocalDate transDate;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
    // Количество разных версий ОС за 30 дней
    @Column(name = "unique_os_versions_30d")
    private Integer uniqueOsVersions30d;
    
    // Количество разных моделей телефона за 30 дней
    @Column(name = "unique_phone_models_30d")
    private Integer uniquePhoneModels30d;
    
    // Модель телефона из последней сессии
    @Column(name = "latest_phone_model")
    private String latestPhoneModel;
    
    // Версия ОС из последней сессии
    @Column(name = "latest_os_version")
    private String latestOsVersion;
    
    // Логины за 7 дней
    @Column(name = "logins_last_7_days")
    private Integer loginsLast7Days;
    
    // Логины за 30 дней
    @Column(name = "logins_last_30_days")
    private Integer loginsLast30Days;
    
    // Средняя частота логинов за 7 дней
    @Column(name = "avg_logins_per_day_7d", precision = 20, scale = 4)
    private BigDecimal avgLoginsPerDay7d;
    
    // Средняя частота логинов за 30 дней
    @Column(name = "avg_logins_per_day_30d", precision = 20, scale = 4)
    private BigDecimal avgLoginsPerDay30d;
    
    // Изменение частоты логинов
    @Column(name = "login_freq_change_ratio", precision = 20, scale = 4)
    private BigDecimal loginFreqChangeRatio;
    
    // Доля логинов 7д от 30д
    @Column(name = "login_ratio_7d_30d", precision = 20, scale = 4)
    private BigDecimal loginRatio7d30d;
    
    // Средний интервал между сессиями (секунды)
    @Column(name = "avg_session_interval_sec", precision = 20, scale = 4)
    private BigDecimal avgSessionIntervalSec;
    
    // Стандартное отклонение интервалов
    @Column(name = "session_interval_std", precision = 20, scale = 4)
    private BigDecimal sessionIntervalStd;
    
    // Дисперсия интервалов
    @Column(name = "session_interval_variance", precision = 20, scale = 4)
    private BigDecimal sessionIntervalVariance;
    
    // Экспоненциально взвешенное среднее
    @Column(name = "exp_weighted_avg_interval", precision = 20, scale = 4)
    private BigDecimal expWeightedAvgInterval;
    
    // Показатель взрывности
    @Column(name = "burstiness_score", precision = 20, scale = 4)
    private BigDecimal burstinessScore;
    
    // Fano factor
    @Column(name = "fano_factor", precision = 20, scale = 4)
    private BigDecimal fanoFactor;
    
    // Z-score интервалов
    @Column(name = "interval_zscore", precision = 20, scale = 4)
    private BigDecimal intervalZscore;
}