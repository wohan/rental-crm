package ru.rentcrm.app.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class FieldEncryption {
    private static final String PREFIX = "enc:v1:";
    private static final String KEY_ENV = "APP_FIELD_ENCRYPTION_KEY";
    private static final int KEY_BYTES = 32;
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static volatile SecretKeySpec cachedKey;

    private FieldEncryption() {
    }

    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    public static String encrypt(String value) {
        if (value == null || value.isBlank() || isEncrypted(value)) {
            return value;
        }
        try {
            byte[] iv = new byte[IV_BYTES];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            ByteBuffer payload = ByteBuffer.allocate(iv.length + encrypted.length);
            payload.put(iv);
            payload.put(encrypted);
            return PREFIX + Base64.getEncoder().encodeToString(payload.array());
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to encrypt sensitive field", ex);
        }
    }

    public static String decrypt(String value) {
        if (value == null || value.isBlank() || !isEncrypted(value)) {
            return value;
        }
        try {
            byte[] payload = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            if (payload.length <= IV_BYTES) {
                throw new IllegalStateException("Encrypted field payload is too short");
            }
            byte[] iv = new byte[IV_BYTES];
            byte[] encrypted = new byte[payload.length - IV_BYTES];
            System.arraycopy(payload, 0, iv, 0, IV_BYTES);
            System.arraycopy(payload, IV_BYTES, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new IllegalStateException("Failed to decrypt sensitive field", ex);
        }
    }

    private static SecretKeySpec key() {
        SecretKeySpec key = cachedKey;
        if (key != null) {
            return key;
        }
        String encoded = System.getenv(KEY_ENV);
        if (encoded == null || encoded.isBlank()) {
            throw new IllegalStateException(KEY_ENV + " is required for sensitive field encryption");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(encoded.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(KEY_ENV + " must be base64-encoded", ex);
        }
        if (bytes.length != KEY_BYTES) {
            throw new IllegalStateException(KEY_ENV + " must decode to 32 bytes");
        }
        SecretKeySpec created = new SecretKeySpec(bytes, "AES");
        cachedKey = created;
        return created;
    }
}
