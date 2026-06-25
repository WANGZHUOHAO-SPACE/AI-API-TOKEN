package com.keybridge.module.wallet.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateRechargeRequest(
        @NotNull @DecimalMin("1.00") @DecimalMax("100000.00") BigDecimal amountUsd,
        @NotBlank String paymentMethod
) {
}
