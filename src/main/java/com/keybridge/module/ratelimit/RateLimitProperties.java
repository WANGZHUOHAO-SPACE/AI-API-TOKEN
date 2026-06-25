package com.keybridge.module.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(long userDailyLimit, long keyMinuteLimit) {

    public RateLimitProperties {
        if (userDailyLimit < 1 || keyMinuteLimit < 1) {
            throw new IllegalArgumentException("Rate limits must be positive");
        }
    }
}
