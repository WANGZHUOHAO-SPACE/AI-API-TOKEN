package com.keybridge.module.provider.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CredentialRequest(
        @NotNull Long providerId,
        @NotBlank @Size(max = 100) String name,
        @Size(min = 8, max = 1000, message = "apiKey 长度必须在 8 到 1000 位之间") String apiKey,
        @PositiveOrZero Integer priority,
        @Positive Integer weight,
        Integer status,
        LocalDateTime expiresAt
) {
}
