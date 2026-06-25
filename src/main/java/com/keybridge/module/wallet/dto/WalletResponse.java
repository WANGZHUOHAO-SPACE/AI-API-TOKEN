package com.keybridge.module.wallet.dto;

import java.math.BigDecimal;

public record WalletResponse(BigDecimal balanceUsd, BigDecimal totalRechargedUsd) {
}
