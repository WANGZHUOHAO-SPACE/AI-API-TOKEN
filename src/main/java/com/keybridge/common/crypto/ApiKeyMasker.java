package com.keybridge.common.crypto;

public final class ApiKeyMasker {

    private static final String MASK = "********";

    private ApiKeyMasker() {
    }

    public static String maskSuffix(String suffix) {
        return suffix == null || suffix.isBlank() ? MASK : MASK + suffix;
    }
}
