package com.keybridge.module.wallet.service;

import com.keybridge.module.wallet.config.PaymentProperties;
import com.keybridge.module.wallet.dto.ExchangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final Duration CACHE_DURATION = Duration.ofMinutes(5);
    private final PaymentProperties properties;
    private volatile ExchangeRateResponse cached;

    public ExchangeRateResponse getUsdCnyRate() {
        ExchangeRateResponse current = cached;
        if (current != null && current.fetchedAt().plus(CACHE_DURATION).isAfter(LocalDateTime.now())) {
            return current;
        }
        synchronized (this) {
            current = cached;
            if (current != null && current.fetchedAt().plus(CACHE_DURATION).isAfter(LocalDateTime.now())) {
                return current;
            }
            try {
                FrankfurterRate response = RestClient.create(properties.exchangeRateUrl())
                        .get()
                        .retrieve()
                        .body(FrankfurterRate.class);
                if (response == null || response.rate() == null || response.rate().signum() <= 0) {
                    throw new IllegalStateException("汇率响应无效");
                }
                cached = new ExchangeRateResponse("USD", "CNY", response.rate(), response.date(),
                        LocalDateTime.now(), "Frankfurter / ECB reference rate");
            } catch (Exception ignored) {
                cached = new ExchangeRateResponse("USD", "CNY", properties.fallbackUsdCnyRate(),
                        LocalDate.now(), LocalDateTime.now(), "系统备用汇率");
            }
            return cached;
        }
    }

    private record FrankfurterRate(LocalDate date, String base, String quote, BigDecimal rate) {
    }
}
