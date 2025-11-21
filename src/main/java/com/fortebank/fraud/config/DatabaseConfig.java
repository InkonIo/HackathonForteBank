package com.fortebank.fraud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Конфигурация базы данных
 * 
 * Включает:
 * - JPA Repositories
 * - Transaction Management
 * - JPA Auditing (@CreatedDate, @LastModifiedDate)
 * 
 * Таблицы будут созданы автоматически из Entity классов
 * (spring.jpa.hibernate.ddl-auto=create-drop)
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.fortebank.fraud")
@EnableTransactionManagement
@EnableJpaAuditing
public class DatabaseConfig {
    
    // Hibernate автоматически создаст таблицы из Entity классов
    // Не нужны миграции Flyway - всё просто!
}