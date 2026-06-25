package com.keybridge.module.statistics.dto;

import lombok.Data;

@Data
public class StatisticsBreakdown {
    private String name;
    private Long requestCount;
    private Long successCount;
    private Long failureCount;
    private Long totalTokens;
    private Long averageDurationMs;
}
