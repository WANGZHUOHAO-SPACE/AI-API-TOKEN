package com.keybridge.common.crypto;

import com.keybridge.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AesGcmService {

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private final SecretKeySpec secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmService(CryptoProperties properties) {
        try {
            byte[] key = MessageDigest.getInstance("SHA-256")
                    .digest(properties.key().getBytes(StandardCharsets.UTF_8));
            this.secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot initialize credential encryption", exception);
        }
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            byte[] result = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception exception) {
            throw new BusinessException(500, "上游密钥加密失败");
        }
    }

    public String decrypt(String cipherText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[decoded.length - IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
            System.arraycopy(decoded, IV_LENGTH, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new BusinessException(500, "上游密钥解密失败");
        }
    }
}
