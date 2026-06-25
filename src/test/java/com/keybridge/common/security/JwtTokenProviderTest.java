package com.keybridge.common.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    @Test
    void shouldCreateAndParseToken() {
        JwtTokenProvider provider = new JwtTokenProvider(
                new JwtProperties("test-secret-that-is-definitely-at-least-32-bytes", 3600));

        String token = provider.createToken("alice", "USER");

        assertThat(provider.isValid(token)).isTrue();
        assertThat(provider.getUsername(token)).isEqualTo("alice");
    }
}
