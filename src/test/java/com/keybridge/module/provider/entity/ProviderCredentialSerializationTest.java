package com.keybridge.module.provider.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProviderCredentialSerializationTest {

    @Test
    void shouldNeverSerializeEncryptedKeyOrRawSuffix() throws Exception {
        ProviderCredential credential = new ProviderCredential();
        credential.setId(1L);
        credential.setEncryptedKey("encrypted-secret-value");
        credential.setKeySuffix("a1b2");

        String json = new ObjectMapper().writeValueAsString(credential);

        assertThat(json).doesNotContain("encryptedKey", "encrypted-secret-value", "keySuffix", "a1b2");
    }
}
