package de.remsfal.common.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Loads, derives and generates the RSA keys used to sign JWTs.
 * <p>
 * Supported key locations:
 * <ul>
 * <li>{@code file:/path/to/key.pem} or an absolute path: read from the file system</li>
 * <li>{@code classpath:key.pem} or a plain name: read from the classpath</li>
 * </ul>
 */
public class KeyLoader {

    public static final String PRIVATE_KEY_FILE_NAME = "privateKey.pem";

    private static final String FILE_PREFIX = "file:";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final int KEY_SIZE = 2048;

    private KeyLoader() {
    }

    public static PrivateKey loadPrivateKey(final String location)
        throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return parsePrivateKey(readKey(location, "Private"));
    }

    public static PublicKey loadPublicKey(final String location)
        throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        return parsePublicKey(readKey(location, "Public"));
    }

    /**
     * Derives the public key from an RSA private key, so only the private key has to be provided as secret.
     */
    public static PublicKey derivePublicKey(final PrivateKey privateKey) throws GeneralSecurityException {
        if (!(privateKey instanceof RSAPrivateCrtKey rsaPrivateKey)) {
            throw new InvalidKeySpecException("Unable to derive public key: RSA private key (PKCS#8) required");
        }
        final RSAPublicKeySpec spec = new RSAPublicKeySpec(rsaPrivateKey.getModulus(),
            rsaPrivateKey.getPublicExponent());
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    /**
     * Generates a new RSA key pair and stores the private key as PKCS#8 PEM file
     * ({@value #PRIVATE_KEY_FILE_NAME}) in the given directory, readable by the owner only.
     *
     * @return the generated private key
     */
    public static PrivateKey generateAndStore(final Path directory) throws IOException, GeneralSecurityException {
        final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(KEY_SIZE);
        final KeyPair keyPair = generator.generateKeyPair();

        Files.createDirectories(directory);
        final Path file = directory.resolve(PRIVATE_KEY_FILE_NAME);
        Files.writeString(file, toPem(keyPair.getPrivate()), StandardCharsets.US_ASCII);
        try {
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException e) {
            // file system without POSIX permissions (e.g. Windows)
        }
        return keyPair.getPrivate();
    }

    static String toPem(final PrivateKey privateKey) {
        final String base64 = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
            .encodeToString(privateKey.getEncoded());
        return "-----BEGIN PRIVATE KEY-----\n" + base64 + "\n-----END PRIVATE KEY-----\n";
    }

    private static String readKey(final String location, final String type) throws IOException {
        if (location == null || location.isBlank()) {
            throw new IllegalArgumentException(type + " key location is not configured");
        }
        if (location.startsWith(FILE_PREFIX) || location.startsWith("/")) {
            final Path path = Path.of(location.startsWith(FILE_PREFIX)
                ? location.substring(FILE_PREFIX.length()) : location);
            if (!Files.isReadable(path)) {
                throw new IllegalArgumentException(type + " key file not found or not readable: " + location);
            }
            return Files.readString(path, StandardCharsets.US_ASCII);
        }
        final String resource = location.startsWith(CLASSPATH_PREFIX)
            ? location.substring(CLASSPATH_PREFIX.length()) : location;
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new IllegalArgumentException(type + " key file not found: " + location);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.US_ASCII);
        }
    }

    private static PrivateKey parsePrivateKey(final String pem)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (pem.contains("BEGIN RSA PRIVATE KEY")) {
            throw new InvalidKeySpecException("PKCS#1 keys are not supported, convert the key to PKCS#8: "
                + "openssl pkcs8 -topk8 -nocrypt -in old.pem -out privateKey.pem");
        }
        final String base64Content = pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");

        final byte[] keyBytes = Base64.getDecoder().decode(base64Content);

        final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private static PublicKey parsePublicKey(final String pem)
        throws NoSuchAlgorithmException, InvalidKeySpecException {
        final String base64Content = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");

        final byte[] keyBytes = Base64.getDecoder().decode(base64Content);

        final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

}
