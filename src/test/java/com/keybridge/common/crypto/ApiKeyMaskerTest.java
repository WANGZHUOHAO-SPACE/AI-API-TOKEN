package com.keybridge.common.crypto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyMaskerTest {

    @Test
    void shouldOnlyExposeStoredSuffix() {
        assertThat(ApiKeyMasker.maskSuffix("9xYz")).isEqualTo("********9xYz");
        assertThat(ApiKeyMasker.maskSuffix(null)).isEqualTo("********");
    }
}
