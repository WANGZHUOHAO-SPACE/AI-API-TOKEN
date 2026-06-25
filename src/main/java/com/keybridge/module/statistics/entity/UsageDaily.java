package com.keybridge.module.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("usage_daily")
public class UsageDaily {
    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate usageDate;
    private Long userId;
    private Long modelId;
    private Long providerId;
    private String providerCode;
    private String modelName;
    private Long requestCount;
    private Long successCount;
    private Long failureCount;
    private Long inputTokens;
    private Long outputTokens;
    private Long totalTokens;
    private Long totalDurationMs;
    private BigDecimal totalCost;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
