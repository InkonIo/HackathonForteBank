package com.fortebank.fraud.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Сущность пользователя системы (сотрудник банка)
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password; // Хранится в зашифрованном виде (BCrypt)
    
    @Column(nullable = false, length = 100)
    private String fullName; // ФИО сотрудника
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // ADMIN, ANALYST, VIEWER
    
    @Column(nullable = false)
    private Boolean enabled = true; // Активен ли пользователь
    
    @Column(nullable = false)
    private Boolean accountNonLocked = true; // Не заблокирован ли
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime lastLoginAt; // Последний вход
    
    /**
     * Роли пользователей системы
     */
    public enum UserRole {
        ADMIN,    // Полный доступ
        ANALYST,  // Может просматривать и анализировать
        VIEWER    // Только просмотр
    }
}