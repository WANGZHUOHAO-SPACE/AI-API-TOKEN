package com.keybridge.module.proxy.dto;

public record ChatProxyResponse(
        String requestId,
        String mode,
        String provider,
        String model,
        String content,
        String finishReason,
        long durationMs,
        ChatUsage usage
) {
}
