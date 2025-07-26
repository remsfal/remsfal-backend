package de.remsfal.chat.control;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
public class FileStorageServiceTest {

    @Inject
    FileStorageService fileStorageService;

    private static final String BUCKET_NAME = "test-bucket";

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "%dev.quarkus.minio.secret-key")
    String secretKey;

    @ConfigProperty(name = "%dev.quarkus.minio.access-key")
    String accessKey;

    @ConfigProperty(name = "%dev.quarkus.minio.host")
    String host;

    @ConfigProperty(name = "%dev.quarkus.minio.port")
    String port;

    @BeforeEach
    public void setup(){
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(BUCKET_NAME).build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(BUCKET_NAME).build());
            }
        } catch (Exception e) {
            fail("Failed to initialize MinIO bucket: " + e.getMessage());
        }
    }

    @AfterEach
    public void cleanup() throws Exception {
        Iterable<Result<Item>> results = fileStorageService.listObjects(BUCKET_NAME);
        for (Result<Item> result : results) {
            Item item = result.get();
            fileStorageService.deleteObject(BUCKET_NAME, item.objectName());
        }
    }
    @Test
    public void testUploadFile_SUCCESS() throws Exception {
        String fileName = "test-image.png";
        String contentType = "image/png";
        byte[] fileContent = "dummy image content".getBytes();

        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);
        String fileUrl = fileStorageService.uploadFile(BUCKET_NAME, input);

        assertNotNull("File URL should not be null after successful upload", fileUrl);
        boolean fileFound = false;
        for (Result<Item> result : fileStorageService.listObjects(BUCKET_NAME)) {
            Item item = result.get();
            if (fileUrl.contains(item.objectName())) {
                fileFound = true;
                break;
            }
        }
        assertTrue("Uploaded file should be found in the bucket", fileFound);
    }

    @Test
    public void testUploadFile_NoFilePart_Failure() {
        // Simulate a MultipartFormDataInput with no "file" entry
        MultipartFormDataInput input = mock(MultipartFormDataInput.class);
        when(input.getFormDataMap()).thenReturn(Map.of());

        fileStorageService.logger = Logger.getLogger(FileStorageService.class);
        Exception thrown = assertThrows(RuntimeException.class, ()
                -> fileStorageService.uploadFile(BUCKET_NAME, input));
        assertTrue(thrown.getMessage().contains("File is null or empty"));
    }

    @Test
    public void testUploadFile_EmptyFilePart_Failure() {
        // Simulate a MultipartFormDataInput with empty file list
        MultipartFormDataInput input = mock(MultipartFormDataInput.class);
        when(input.getFormDataMap()).thenReturn(Map.of("file", List.of()));

        fileStorageService.logger = Logger.getLogger(FileStorageService.class);
        Exception thrown = assertThrows(RuntimeException.class, ()
                -> fileStorageService.uploadFile(BUCKET_NAME, input));

        assertTrue(thrown.getMessage().contains("File is null or empty"));
    }

    @Test
    public void testUploadFile_InvalidContentType_Failure() throws Exception {
        String fileName = "malicious.exe";
        String contentType = "application/x-msdownload"; // not in allowedTypes
        byte[] fileContent = "dummy content".getBytes();

        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);
        fileStorageService.logger = Logger.getLogger(FileStorageService.class);
        Exception thrown = assertThrows(RuntimeException.class, () ->
                fileStorageService.uploadFile(BUCKET_NAME, input));
        assertTrue(thrown.getMessage().contains("Invalid file type"));
    }
    @Test
    public void testUploadFile_UniqueNaming_SUCCESS() throws Exception {
        String fileName = "test-image.png";
        String contentType = "image/png";
        byte[] fileContent = "dummy image content".getBytes();

        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);
        String fileUrl = fileStorageService.uploadFile(BUCKET_NAME, input);
        String fileUrl2 = fileStorageService.uploadFile(BUCKET_NAME, input);
        assertNotEquals(fileUrl, fileUrl2);
    }

    @Test
    public void testDownloadFile_Success() throws Exception {
        String fileName = "test-download.txt";
        String contentType = "text/plain";
        byte[] fileContent = "This is some text".getBytes();
        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);
        String fileUrl = fileStorageService.uploadFile(BUCKET_NAME, input);
        assertNotNull(fileUrl);
        try (InputStream downloaded = fileStorageService.downloadFile(BUCKET_NAME, fileName)) {
            assertNotNull("Downloaded file input stream should not be null", downloaded);
            byte[] downloadedBytes = downloaded.readAllBytes();
            assertTrue(new String(downloadedBytes).contains("This is some text"));
        }
    }

    @Test
    public void testDownloadFile_NotFound_Failure() {
        Exception thrown = assertThrows(Exception.class, () ->
                fileStorageService.downloadFile(BUCKET_NAME, "non-existent-file.txt"));
        assertTrue(thrown.getMessage().contains("File does not exist"));
    }

    @Test
    public void testDownloadFile_BucketNotFound_Failure() {
        Exception thrown = assertThrows(RuntimeException.class, () ->
                fileStorageService.downloadFile("non-existent-bucket", "file.txt"));
        assertTrue(thrown.getMessage().contains("Bucket does not exist"));
    }

    @Test
    public void testGetContentType_Success() throws Exception {
        String fileName = "content-type-test.pdf";
        String contentType = "application/pdf";
        byte[] fileContent = "Fake PDF content".getBytes();
        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);
        fileStorageService.uploadFile(BUCKET_NAME, input);
        String returnedContentType = fileStorageService.getContentType(BUCKET_NAME, fileName);
        assertEquals("application/pdf", returnedContentType);
    }

    @Test
    public void testGetContentType_FileNotFound_Failure() {
        Exception thrown = assertThrows(Exception.class, () ->
                fileStorageService.getContentType(BUCKET_NAME, "ghost-file.txt"));
        assertTrue(thrown.getMessage().contains("File does not exist"));
    }

    @Test
    public void testGetContentType_BucketNotFound_Failure() {
        Exception thrown = assertThrows(Exception.class, () ->
                fileStorageService.getContentType("non-existent-bucket", "file.txt"));
        assertTrue(thrown.getMessage().contains("Bucket does not exist"));
    }

    @Test
    public void testGetContentType_InvalidFileName_Failure() {
        Exception thrown = assertThrows(Exception.class, () ->
                fileStorageService.getContentType(BUCKET_NAME, null));
        assertTrue(thrown.getMessage().contains("Error occurred while retrieving file metadata"));
    }

    @Test
    public void testListObjects_Success() throws Exception {
        byte[] content = "some content".getBytes();
        fileStorageService.uploadFile(BUCKET_NAME,
                createMultipartFormDataInput("file1.png", "image/png", content));
        fileStorageService.uploadFile(BUCKET_NAME,
                createMultipartFormDataInput("file2.png", "image/png", content));

        Iterable<Result<Item>> objects = fileStorageService.listObjects(BUCKET_NAME);
        int count = 0;
        for (Result<Item> r : objects) {
            r.get();
            count++;
        }
        assertTrue("Should list at least two objects", count >= 2);
    }

    @Test
    public void testListObjects_bucketNotFound_FAILURE() {
        Exception thrown = assertThrows(RuntimeException.class, () ->
                fileStorageService.listObjects("non-existent-bucket"));
        assertTrue(thrown.getMessage().contains("Bucket does not exist"));
    }

    @Test
    public void testDeleteObject_Success() throws Exception {
        String fileName = "file-to-delete.txt";
        String contentType = "text/plain";
        byte[] fileContent = "To be deleted".getBytes();
        MultipartFormDataInput input = createMultipartFormDataInput(fileName, contentType, fileContent);
        fileStorageService.uploadFile(BUCKET_NAME, input);

        fileStorageService.deleteObject(BUCKET_NAME, fileName);

        boolean found = false;
        for (Result<Item> r : fileStorageService.listObjects(BUCKET_NAME)) {
            if (r.get().objectName().equals(fileName)) {
                found = true;
                break;
            }
        }
        assertFalse("Object should have been deleted", found);
    }

    @Test
    public void testDeleteObject_NotFound_FAILURE() {
        String fileName = "non-existent-file.txt";
        Exception thrown = assertThrows(Exception.class, () ->
                fileStorageService.deleteObject(BUCKET_NAME, fileName));
        assertTrue(thrown.getMessage().contains("File does not exist"));
    }

    @Test
    public void testDeleteObject_InvalidInput_FAILURE() {
        Exception thrown = assertThrows(Exception.class, () ->
                fileStorageService.deleteObject(null, null));
        assertTrue(thrown.getMessage().contains("Error occurred while deleting object"));
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




