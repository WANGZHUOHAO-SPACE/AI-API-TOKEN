package com.keybridge.common.crypto;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.crypto")
public record CryptoProperties(String key) {
}
