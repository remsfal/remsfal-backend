package de.remsfal.ticketing.entity.dao.storage;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.io.InputStream;

/**
 * MinIO implementation of StorageClient.
 */
public class MinioStorageClient implements StorageClient {

    private static final long DEFAULT_OBJECT_SIZE = -1;
    private static final long DEFAULT_PART_SIZE = 5L * 1024L * 1024L;

    private final MinioClient minioClient;
    private final String bucketName;
    private final Logger logger;

    public MinioStorageClient(MinioClient minioClient, String bucketName, Logger logger) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
        this.logger = logger;
    }

    @Override
    public void initialize() throws Exception {
        logger.info("Initializing MinIO storage client...");
        boolean bucketExists = minioClient.bucketExists(
            BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());
        if (!bucketExists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            logger.infov("MinIO bucket '{0}' was created.", bucketName);
        } else {
            logger.infov("MinIO bucket '{0}' already exists.", bucketName);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, MediaType contentType) throws Exception {
        logger.infov("Uploading file {0} to MinIO bucket {1}", fileName, bucketName);
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .stream(inputStream, DEFAULT_OBJECT_SIZE, DEFAULT_PART_SIZE)
                .contentType(contentType.toString())
                .build());
        return fileName;
    }

    @Override
    public InputStream downloadFile(String fileName) throws Exception {
        try {
            logger.infov("Downloading file {0} from MinIO bucket {1}", fileName, bucketName);
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                logger.warnv("Object {0} does not exist in MinIO bucket {1}", fileName, bucketName);
                throw new NotFoundException(e);
            }
            throw e;
        }
    }

    @Override
    public void deleteFile(String fileName) throws Exception {
        logger.infov("Deleting file {0} from MinIO bucket {1}", fileName, bucketName);
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build());
    }

    @Override
    public boolean fileExists(String fileName) throws Exception {
        try {
            logger.debugv("Checking if object {0} exists in MinIO bucket {1}", fileName, bucketName);
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                return false;
            }
            throw e;
        }
    }
}
