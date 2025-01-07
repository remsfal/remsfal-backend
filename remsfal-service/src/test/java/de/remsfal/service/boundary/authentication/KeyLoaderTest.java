package de.remsfal.service.boundary.authentication;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
class KeyLoaderTest {

    @Test
    void testLoadPrivateKey() throws Exception {
        PrivateKey privateKey = KeyLoader.loadPrivateKey("privateKey.pem");
        assertNotNull(privateKey, "Private key should not be null");
    }

    @Test
    void testLoadPublicKey() throws Exception {
        // Beispiel-Datei: resources/public_key.pem
        PublicKey publicKey = KeyLoader.loadPublicKey("publicKey.pem");
        assertNotNull(publicKey, "Public key should not be null");
    }

    @Test
    void testLoadPrivateKeyFileNotFound() {
        // Test für einen nicht existierenden Schlüssel
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            KeyLoader.loadPrivateKey("nonexistent_private_key.pem");
        });
        assertNotNull(exception.getMessage());
    }

    @Test
    void testLoadPublicKeyFileNotFound() {
        // Test für einen nicht existierenden Schlüssel
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            KeyLoader.loadPublicKey("nonexistent_public_key.pem");
        });
        assertNotNull(exception.getMessage());
    }
}
