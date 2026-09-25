package de.remsfal.common.authentication;

import org.junit.jupiter.api.Test;

import de.remsfal.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;

import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class KeyLoaderTest extends AbstractTest {

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

    @Test
    void testLoadPrivateKeyWithClasspathPrefix() throws Exception {
        assertNotNull(KeyLoader.loadPrivateKey("classpath:privateKey.pem"));
    }

    @Test
    void testDerivePublicKeyMatchesKeyPair() throws Exception {
        final PrivateKey privateKey = KeyLoader.loadPrivateKey("privateKey.pem");
        final PublicKey expected = KeyLoader.loadPublicKey("publicKey.pem");

        final PublicKey derived = KeyLoader.derivePublicKey(privateKey);

        assertEquals(expected, derived);
        assertTrue(verifies(privateKey, derived));
    }

    @Test
    void testGenerateAndStore(@TempDir final Path directory) throws Exception {
        final PrivateKey generated = KeyLoader.generateAndStore(directory.resolve("keys"));
        final Path file = directory.resolve("keys").resolve(KeyLoader.PRIVATE_KEY_FILE_NAME);

        assertTrue(Files.readString(file).startsWith("-----BEGIN PRIVATE KEY-----"));
        assertEquals(PosixFilePermissions.fromString("rw-------"), Files.getPosixFilePermissions(file));

        final PrivateKey loaded = KeyLoader.loadPrivateKey("file:" + file);
        assertEquals(generated, loaded);
        assertEquals(generated, KeyLoader.loadPrivateKey(file.toString()));
        assertTrue(verifies(loaded, KeyLoader.derivePublicKey(loaded)));
    }

    @Test
    void testLoadPrivateKeyFromMissingFile(@TempDir final Path directory) {
        final String location = "file:" + directory.resolve("missing.pem");
        final Exception exception = assertThrows(IllegalArgumentException.class,
            () -> KeyLoader.loadPrivateKey(location));
        assertTrue(exception.getMessage().contains(location), exception.getMessage());
    }

    @Test
    void testLoadPkcs1PrivateKeyIsRejected(@TempDir final Path directory) throws Exception {
        final Path file = directory.resolve("pkcs1.pem");
        Files.writeString(file, "-----BEGIN RSA PRIVATE KEY-----\nAAAA\n-----END RSA PRIVATE KEY-----\n");
        final Exception exception = assertThrows(InvalidKeySpecException.class,
            () -> KeyLoader.loadPrivateKey("file:" + file));
        assertTrue(exception.getMessage().contains("PKCS#8"), exception.getMessage());
    }

    private static boolean verifies(final PrivateKey privateKey, final PublicKey publicKey) throws Exception {
        final byte[] data = "remsfal".getBytes();
        final Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(privateKey);
        signer.update(data);
        final byte[] signature = signer.sign();
        final Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(data);
        return verifier.verify(signature);
    }

}
