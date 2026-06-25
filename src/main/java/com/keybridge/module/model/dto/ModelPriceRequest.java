package com.keybridge.module.model.dto;

import jakarta.validation.constraints.Size;

public record ModelPriceRequest(
        @Size(max = 120) String inputPrice,
        @Size(max = 120) String cachedPrice,
        @Size(max = 120) String outputPrice,
        @Size(max = 500) String extraPrice
) {
}
