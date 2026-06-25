package com.keybridge.module.token.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record CreateAccessTokenRequest(
        @NotBlank @Size(max = 100) String name,
        LocalDateTime expiresAt,
        @Positive Long requestLimit,
        @Positive Long tokenLimit,
        List<Long> modelIds
) {
}
