package com.fortebank.fraud.batch.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "filename", nullable = false)
    private String filename;
    
    @Column(name = "total_records")
    private Integer totalRecords;
    
    @Column(name = "processed_records")
    @Builder.Default
    private Integer processedRecords = 0;
    
    @Column(name = "failed_records")
    @Builder.Default
    private Integer failedRecords = 0;
    
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BatchJobStatus status = BatchJobStatus.PENDING;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}