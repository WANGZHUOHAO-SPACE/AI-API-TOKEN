package com.keybridge.module.log.dto;

import java.math.BigDecimal;

public record InvocationLogCommand(
        String requestId,
        Long userId,
        Long providerId,
        String providerCode,
        Long credentialId,
        String modelName,
        int inputTokens,
        int outputTokens,
        int totalTokens,
        BigDecimal cost,
        long durationMs,
        int statusCode,
        boolean success,
        String errorMessage
) {
}
