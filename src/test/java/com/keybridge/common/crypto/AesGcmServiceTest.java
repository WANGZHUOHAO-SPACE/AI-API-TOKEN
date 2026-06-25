package com.keybridge.common.crypto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AesGcmServiceTest {

    @Test
    void shouldEncryptWithRandomIvAndDecrypt() {
        AesGcmService service = new AesGcmService(new CryptoProperties("test-encryption-key"));

        String first = service.encrypt("sk-upstream-secret");
        String second = service.encrypt("sk-upstream-secret");

        assertThat(first).isNotEqualTo(second);
        assertThat(service.decrypt(first)).isEqualTo("sk-upstream-secret");
        assertThat(service.decrypt(second)).isEqualTo("sk-upstream-secret");
    }
}
