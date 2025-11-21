package com.fortebank.fraud.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskFactorDTO {
    private String name;
    private String description;
    private Integer score;
    private Double weight;
}