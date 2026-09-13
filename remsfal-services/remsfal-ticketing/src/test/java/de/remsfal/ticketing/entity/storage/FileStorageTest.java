package de.remsfal.ticketing.entity.storage;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the generic-{@code S3Exception}/unexpected-exception branches in {@link FileStorage}
 * that a real S3-compatible backend (LocalStack) never actually returns (it always throws the
 * typed {@code NoSuchKeyException}/{@code NoSuchBucketException}), so they can't be exercised
 * by the LocalStack-backed tests in {@code FileStorageControllerTest}/{@code AbstractTicketingTest}.
 */
class FileStorageTest {

    private static final String BUCKET_NAME = "test-bucket";

    private S3Client s3Client;
    private FileStorage storage;

    @BeforeEach
    void setUp() {
        s3Client = mock(S3Client.class);
        storage = new FileStorage();
        storage.bucketName = BUCKET_NAME;
        storage.logger = Logger.getLogger(FileStorage.class);
        storage.s3Client = s3Client;
    }

    @Test
    void onStartup_bucketAlreadyExists_doesNotCreateBucket() throws Exception {
        storage.onStartup(null);

        verify(s3Client, never()).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void onStartup_noSuchBucketException_createsBucket() throws Exception {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(NoSuchBucketException.builder().message("no bucket").build());

        storage.onStartup(null);

        verify(s3Client, times(1)).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void onStartup_genericS3ExceptionWith404_createsBucket() throws Exception {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(S3Exception.builder().statusCode(404).message("not found").build());

        storage.onStartup(null);

        verify(s3Client, times(1)).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void onStartup_genericS3ExceptionNon404_rethrowsAndDoesNotCreateBucket() {
        when(s3Client.headBucket(any(HeadBucketRequest.class)))
            .thenThrow(S3Exception.builder().statusCode(500).message("boom").build());

        S3Exception thrown = assertThrows(S3Exception.class, () -> storage.onStartup(null));

        assertEquals(500, thrown.statusCode());
        verify(s3Client, never()).createBucket(any(CreateBucketRequest.class));
    }

    @Test
    void downloadFile_genericS3ExceptionWith404_throwsNotFound() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(S3Exception.builder().statusCode(404).message("not found").build());

        assertThrows(NotFoundException.class, () -> storage.downloadFile("missing.txt"));
    }

    @Test
    void downloadFile_genericS3ExceptionNon404_throwsInternalServerError() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(S3Exception.builder().statusCode(500).message("boom").build());

        assertThrows(InternalServerErrorException.class, () -> storage.downloadFile("file.txt"));
    }

    @Test
    void downloadFile_unexpectedException_throwsInternalServerError() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
            .thenThrow(new RuntimeException("network error"));

        assertThrows(InternalServerErrorException.class, () -> storage.downloadFile("file.txt"));
    }

    @Test
    void deleteFile_exception_throwsInternalServerError() {
        doThrow(new RuntimeException("boom")).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        assertThrows(InternalServerErrorException.class, () -> storage.deleteFile("file.txt"));
    }

    @Test
    void uploadFile_putObjectFails_throwsInternalServerError() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
            .thenThrow(NoSuchKeyException.builder().message("not found").build());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("boom"));

        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        assertThrows(InternalServerErrorException.class,
            () -> storage.uploadFile(inputStream, "file.txt", MediaType.TEXT_PLAIN_TYPE));
    }

    @Test
    void uploadFile_existenceCheckGenericS3Exception404_stillUploadsWithOriginalName() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
            .thenThrow(S3Exception.builder().statusCode(404).message("not found").build());

        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        String result = storage.uploadFile(inputStream, "file.txt", MediaType.TEXT_PLAIN_TYPE);

        assertEquals("file.txt", result);
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFile_existenceCheckGenericS3ExceptionNon404_throwsInternalServerError() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
            .thenThrow(S3Exception.builder().statusCode(500).message("boom").build());

        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        assertThrows(InternalServerErrorException.class,
            () -> storage.uploadFile(inputStream, "file.txt", MediaType.TEXT_PLAIN_TYPE));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFile_existenceCheckUnexpectedException_throwsInternalServerError() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
            .thenThrow(new RuntimeException("network error"));

        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        assertThrows(InternalServerErrorException.class,
            () -> storage.uploadFile(inputStream, "file.txt", MediaType.TEXT_PLAIN_TYPE));
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadFile_multipleCollisions_incrementsCounterUntilFree() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
            .thenReturn(null)
            .thenReturn(null)
            .thenThrow(NoSuchKeyException.builder().message("not found").build());

        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        String result = storage.uploadFile(inputStream, "file.txt", MediaType.TEXT_PLAIN_TYPE);

        assertEquals("file(2).txt", result);
    }

    @Test
    void uploadFile_collisionOnFileNameWithoutExtension_generatesSuffixedName() {
        when(s3Client.headObject(any(HeadObjectRequest.class)))
            .thenReturn(null)
            .thenThrow(NoSuchKeyException.builder().message("not found").build());

        InputStream inputStream = new ByteArrayInputStream("data".getBytes());

        String result = storage.uploadFile(inputStream, "file", MediaType.TEXT_PLAIN_TYPE);

        assertEquals("file(1)", result);
    }
}
