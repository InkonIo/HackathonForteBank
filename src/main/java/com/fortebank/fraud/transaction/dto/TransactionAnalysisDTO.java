package com.fortebank.fraud.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAnalysisDTO {
    private Long transactionId;
    private String customerId;
    private Double fraudProbability;
    private Boolean isFraud;
    private String decision; // BLOCK, APPROVE, REVIEW
    private Integer riskScore;
    private List<RiskFactorDTO> riskFactors;
    private String aiExplanation;
    private String recommendations;
    private LocalDateTime analyzedAt;
}