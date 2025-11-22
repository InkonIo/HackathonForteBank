package com.fortebank.fraud.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceUsageDTO {
    private String deviceModel;
    private String osVersion;
    private Integer usageCount;
    private String lastUsed;
}
