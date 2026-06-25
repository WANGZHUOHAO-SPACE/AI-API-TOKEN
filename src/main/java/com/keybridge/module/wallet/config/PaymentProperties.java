package com.keybridge.module.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "app.payment")
public record PaymentProperties(
        String usdtAddress,
        String usdtNetwork,
        String exchangeRateUrl,
        BigDecimal fallbackUsdCnyRate
) {
}
