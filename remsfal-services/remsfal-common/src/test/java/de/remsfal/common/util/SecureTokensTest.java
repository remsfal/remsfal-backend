package de.remsfal.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SecureTokensTest {

    @Test
    void randomBase64Url_SUCCESS_urlSafeWithoutPadding() {
        final String token = SecureTokens.randomBase64Url(32);
        // 32 bytes are encoded as 43 characters without padding
        assertEquals(43, token.length());
        assertTrue(token.matches("[A-Za-z0-9_-]+"));
        assertNotEquals(token, SecureTokens.randomBase64Url(32));
    }

    @Test
    void randomDigits_SUCCESS_onlyDigitsOfGivenLength() {
        final String code = SecureTokens.randomDigits(8);
        assertTrue(code.matches("\\d{8}"));
        assertEquals("", SecureTokens.randomDigits(0));
    }

    @Test
    void sha256Hex_SUCCESS_knownDigest() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            SecureTokens.sha256Hex("abc"));
    }

    @Test
    void isEqual_SUCCESS_comparesValues() {
        assertTrue(SecureTokens.isEqual("12345678", "12345678"));
        assertFalse(SecureTokens.isEqual("12345678", "87654321"));
        assertFalse(SecureTokens.isEqual(null, "12345678"));
        assertFalse(SecureTokens.isEqual("12345678", null));
    }

}
