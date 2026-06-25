package com.keybridge.module.proxy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatProxyRequest(
        @NotBlank @Size(max = 50) String provider,
        @NotBlank @Size(max = 150) String model,
        @NotNull Long keyId,
        @NotBlank @Size(max = 32000) String prompt
) {
}
