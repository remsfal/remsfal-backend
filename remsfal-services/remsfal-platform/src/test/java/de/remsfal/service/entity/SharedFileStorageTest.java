package de.remsfal.service.entity;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

import de.remsfal.common.entity.storage.FileStorage;
import de.remsfal.test.AbstractTest;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Verifies that the service starts with S3 (Dev Services) and uses the shared bucket.
 */
@QuarkusTest
class SharedFileStorageTest extends AbstractTest {

    @Inject
    FileStorage storage;

    @Test
    void getBucketName_SUCCESS_sharedBucketIsUsed() {
        assertEquals("remsfal", storage.getBucketName());
    }

    @Test
    void putFile_SUCCESS_fileCanBeDownloadedAndDeleted() throws IOException {
        final String key = "test/" + UUID.randomUUID() + "/file.txt";
        final byte[] content = "shared storage".getBytes(StandardCharsets.UTF_8);

        storage.putFile(key, content, MediaType.TEXT_PLAIN_TYPE);
        assertTrue(storage.fileExists(key));
        try (InputStream download = storage.downloadFile(key)) {
            assertArrayEquals(content, download.readAllBytes());
        }

        storage.deleteFile(key);
        assertFalse(storage.fileExists(key));
        assertThrows(NotFoundException.class, () -> storage.downloadFile(key));
    }

}
