package com.fortebank.fraud.batch.repository;

import com.fortebank.fraud.batch.entity.BatchJob;
import com.fortebank.fraud.batch.entity.BatchJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BatchJobRepository extends JpaRepository<BatchJob, Long> {
    
    List<BatchJob> findByStatus(BatchJobStatus status);
    
    List<BatchJob> findByCreatedByOrderByCreatedAtDesc(String createdBy);
}