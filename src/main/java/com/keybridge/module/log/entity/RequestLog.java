package com.keybridge.module.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("request_log")
public class RequestLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String requestId;
    private Long userId;
    private Long accessTokenId;
    private Long modelId;
    private Long providerId;
    private String providerCode;
    private String modelName;
    private Long credentialId;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;
    private BigDecimal cost;
    private Long durationMs;
    private Integer statusCode;
    private Integer success;
    private String errorMessage;
    private LocalDateTime createdAt;
}
