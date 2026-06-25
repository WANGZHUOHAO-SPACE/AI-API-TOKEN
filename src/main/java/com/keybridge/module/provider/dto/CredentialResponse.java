package com.keybridge.module.provider.dto;

import java.time.LocalDateTime;

public record CredentialResponse(
        Long id,
        Long userId,
        String ownerUsername,
        Long providerId,
        String name,
        String maskedKey,
        Integer priority,
        Integer weight,
        Integer status,
        Integer failureCount,
        LocalDateTime lastCheckedAt,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
