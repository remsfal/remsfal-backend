package de.remsfal.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Cryptographically secure random values and hashes for tokens, nonces and one-time codes.
 */
public final class SecureTokens {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private SecureTokens() {
    }

    /** Random bytes encoded as URL-safe Base64 without padding. */
    public static String randomBase64Url(final int numBytes) {
        final byte[] bytes = new byte[numBytes];
        RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }

    /** Random numeric code of the given length, padded with leading zeros. */
    public static String randomDigits(final int length) {
        final StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }

    /** The SHA-256 hash (hex encoded) of the given value. */
    public static String sha256Hex(final String value) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** Compares two strings in constant time to prevent timing attacks. */
    public static boolean isEqual(final String expected, final String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
            actual.getBytes(StandardCharsets.UTF_8));
    }

}
