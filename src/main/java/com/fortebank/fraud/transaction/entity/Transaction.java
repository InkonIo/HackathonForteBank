package com.fortebank.fraud.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_transaction_datetime", columnList = "transaction_datetime"),
    @Index(name = "idx_is_fraud", columnList = "is_fraud"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "transaction_id", unique = true, nullable = false, length = 100)
    private String transactionId; // docno
    
    @Column(name = "customer_id", nullable = false, length = 100)
    private String customerId; // cs_Clnt_Id
    
    @Column(name = "transaction_datetime", nullable = false)
    private LocalDateTime transactionDateTime; // transdatetime
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; // amount
    
    @Column(name = "recipient_id", nullable = false, length = 255)
    private String recipientId; // direction
    
    @Column(name = "is_fraud", nullable = false)
    private Boolean isFraud; // target
    
    @Column(name = "fraud_probability")
    private Double fraudProbability;
    
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;
    
    @Column(name = "batch_id")
    private Long batchId;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}