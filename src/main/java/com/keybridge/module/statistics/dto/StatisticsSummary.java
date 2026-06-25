package com.keybridge.module.statistics.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatisticsSummary {
    private Long requestCount;
    private Long successCount;
    private Long failureCount;
    private Long totalTokens;
    private Long totalDurationMs;
    private BigDecimal averageDurationMs;
    private BigDecimal successRate;
}
