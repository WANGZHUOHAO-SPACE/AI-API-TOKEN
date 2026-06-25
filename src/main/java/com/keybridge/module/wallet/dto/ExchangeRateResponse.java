package com.keybridge.module.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExchangeRateResponse(
        String base,
        String quote,
        BigDecimal rate,
        LocalDate rateDate,
        LocalDateTime fetchedAt,
        String source
) {
}
