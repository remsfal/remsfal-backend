package de.remsfal.ticketing.control;

import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.messages.Item;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.storage.FileStorage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.jboss.logging.Logger;

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@QuarkusTest
public class FileStorageServiceTest extends AbstractTicketingTest {

    @Inject
    FileStorageController fileStorageController;

    @Test
    public void testUploadFile_SUCCESS() throws Exception {
        String fileName = "test-image.png";
        MediaType contentType = MediaType.valueOf("image/png");
        byte[] fileContent = "dummy image content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        String fileUrl = fileStorageController.uploadFile(inputStream, fileName, contentType);

        assertNotNull(fileUrl, "File URL should not be null after successful upload");
    }

    @Test
    public void testUploadFile_InvalidContentType_Failure() throws Exception {
        String fileName = "malicious.exe";
        MediaType contentType = MediaType.valueOf("application/x-msdownload");
        byte[] fileContent = "dummy content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        fileStorageController.logger = Logger.getLogger(FileStorageController.class);
        Exception thrown = assertThrows(RuntimeException.class, 
            () -> fileStorageController.uploadFile(inputStream, fileName, contentType));
        assertTrue(thrown.getMessage().contains("Invalid file type"));
    }

    @Test
    public void testUploadFile_UniqueNaming_SUCCESS() throws Exception {
        String fileName = "test-image.png";
        MediaType contentType = MediaType.valueOf("image/png");
        byte[] fileContent = "dummy image content".getBytes();
        
        InputStream inputStream1 = new ByteArrayInputStream(fileContent);
        String fileUrl = fileStorageController.uploadFile(inputStream1, fileName, contentType);
        
        InputStream inputStream2 = new ByteArrayInputStream(fileContent);
        String fileUrl2 = fileStorageController.uploadFile(inputStream2, fileName, contentType);
        
        assertNotEquals(fileUrl, fileUrl2);
    }

    @Test
    public void testDownloadFile_Success() throws Exception {
        String fileName = "test-download.txt";
        MediaType contentType = MediaType.valueOf("text/plain");
        byte[] fileContent = "This is some text".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        
        String fileUrl = fileStorageController.uploadFile(inputStream, fileName, contentType);
        assertNotNull(fileUrl);
        
        try (InputStream downloaded = fileStorageController.downloadFile(fileName)) {
            assertNotNull(downloaded, "Downloaded file input stream should not be null");
            byte[] downloadedBytes = downloaded.readAllBytes();
            assertTrue(new String(downloadedBytes).contains("This is some text"));
        }
    }

    @Test
    public void testDownloadFile_NotFound_Failure() {
        assertThrows(
            NotFoundException.class,
            () -> fileStorageController.downloadFile("non-existent-file.txt"));
    }

    @Test
    public void testDeleteObject_Success() throws Exception {
        String fileName = "file-to-delete.txt";
        MediaType contentType = MediaType.valueOf("text/plain");
        byte[] fileContent = "To be deleted".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);
        
        fileStorageController.uploadFile(inputStream, fileName, contentType);

        fileStorageController.deleteFile(fileName);

        boolean found = false;
        for (Result<Item> r : minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(FileStorage.DEFAULT_BUCKET_NAME)
                .build())) {
            if (r.get().objectName().equals(fileName)) {
                found = true;
                break;
            }
        }
        assertFalse(found, "Object should have been deleted");
    }

    @Test
    public void testIsValidContentType_Valid() {
        assertTrue(fileStorageController.isValidContentType("image/png"));
        assertTrue(fileStorageController.isValidContentType("application/pdf"));
        assertTrue(fileStorageController.isValidContentType("text/plain"));
    }

    @Test
    public void testIsValidContentType_Invalid() {
        assertFalse(fileStorageController.isValidContentType("application/x-msdownload"));
        assertFalse(fileStorageController.isValidContentType("video/mp4"));
    }
}
