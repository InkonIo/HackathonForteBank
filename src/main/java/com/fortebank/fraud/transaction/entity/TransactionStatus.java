package com.fortebank.fraud.transaction.entity;

public enum TransactionStatus {
    PENDING,      // Ожидает анализа
    ANALYZING,    // В процессе анализа
    ANALYZED,     // Проанализирована
    BLOCKED,      // Заблокирована
    APPROVED,     // Одобрена
    REVIEW        // Требует ручной проверки
}