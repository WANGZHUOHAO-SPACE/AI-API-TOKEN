package com.keybridge.module.proxy.config;

import com.keybridge.common.exception.BusinessException;

import java.util.Locale;

public enum ProxyMode {
    MOCK,
    OPENAI_COMPATIBLE;

    public static ProxyMode from(String value) {
        try {
            return value == null ? MOCK : valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(500, "不支持的转发模式: " + value);
        }
    }
}
