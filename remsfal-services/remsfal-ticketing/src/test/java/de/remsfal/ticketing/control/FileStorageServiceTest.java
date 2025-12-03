package de.remsfal.ticketing.control;

import io.minio.ListObjectsArgs;
import io.minio.Result;
import io.minio.messages.Item;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.jupiter.api.Test;

import de.remsfal.ticketing.AbstractTicketingTest;
import de.remsfal.ticketing.entity.dao.FileStorage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Map;
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
        String contentType = "image/png";
        byte[] fileContent = "dummy image content".getBytes();

        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);
        String fileUrl = fileStorageController.uploadFile(input);

        assertNotNull("File URL should not be null after successful upload", fileUrl);
    }

    @Test
    public void testUploadFile_NoFilePart_Failure() {
        // Simulate a MultipartFormDataInput with no "file" entry
        MultipartFormDataInput input = mock(MultipartFormDataInput.class);
        when(input.getFormDataMap()).thenReturn(Map.of());

        fileStorageController.logger = Logger.getLogger(FileStorageController.class);
        Exception thrown = assertThrows(RuntimeException.class, () -> fileStorageController.uploadFile(input));
        assertTrue(thrown.getMessage().contains("File is null or empty"));
    }

    @Test
    public void testUploadFile_EmptyFilePart_Failure() {
        // Simulate a MultipartFormDataInput with empty file list
        MultipartFormDataInput input = mock(MultipartFormDataInput.class);
        when(input.getFormDataMap()).thenReturn(Map.of("file", List.of()));

        fileStorageController.logger = Logger.getLogger(FileStorageController.class);
        Exception thrown = assertThrows(RuntimeException.class, () -> fileStorageController.uploadFile(input));

        assertTrue(thrown.getMessage().contains("File is null or empty"));
    }

    @Test
    public void testUploadFile_InvalidContentType_Failure() throws Exception {
        String fileName = "malicious.exe";
        String contentType = "application/x-msdownload"; // not in allowedTypes
        byte[] fileContent = "dummy content".getBytes();

        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);
        fileStorageController.logger = Logger.getLogger(FileStorageController.class);
        Exception thrown = assertThrows(RuntimeException.class, () -> fileStorageController.uploadFile(input));
        assertTrue(thrown.getMessage().contains("Invalid file type"));
    }

    @Test
    public void testUploadFile_UniqueNaming_SUCCESS() throws Exception {
        String fileName = "test-image.png";
        String contentType = "image/png";
        byte[] fileContent = "dummy image content".getBytes();

        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);
        String fileUrl = fileStorageController.uploadFile(input);
        String fileUrl2 = fileStorageController.uploadFile(input);
        assertNotEquals(fileUrl, fileUrl2);
    }

    @Test
    public void testDownloadFile_Success() throws Exception {
        String fileName = "test-download.txt";
        String contentType = "text/plain";
        byte[] fileContent = "This is some text".getBytes();
        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);
        String fileUrl = fileStorageController.uploadFile(input);
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
        String contentType = "text/plain";
        byte[] fileContent = "To be deleted".getBytes();
        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);
        fileStorageController.uploadFile(input);

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

    private MultipartFormDataInput createMultipartFormDataInput(String fileName, String contentType, byte[] content)
        throws Exception {
        MultipartFormDataInput input = mock(MultipartFormDataInput.class);
        InputPart inputPart = mock(InputPart.class);

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.add("Content-Disposition", "form-data; name=\"file\"; filename=\"" + fileName + "\"");

        when(inputPart.getHeaders()).thenReturn(headers);
        when(inputPart.getMediaType()).thenReturn(MediaType.valueOf(contentType));
        when(inputPart.getBody(InputStream.class, null)).thenReturn(new ByteArrayInputStream(content));
        when(input.getFormDataMap()).thenReturn(Map.of("file", List.of(inputPart)));

        return input;
    }

}
