package com.keybridge.module.statistics.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StatisticsTrendPoint {
    private LocalDate date;
    private Long requestCount;
    private Long successCount;
    private Long failureCount;
    private Long totalTokens;
    private Long averageDurationMs;
}
