package de.remsfal.chat.entity.dao;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;

import io.minio.errors.ErrorResponseException;
import java.io.InputStream;

import org.jboss.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class FileStorage {

    public static final long DEFAULT_OBJECT_SIZE = -1;

    public static final long DEFAULT_PART_SIZE = 5L * 1024L * 1024L;

    public static final String DEFAULT_BUCKET_NAME = "remsfal-ticketing";

    @ConfigProperty(name = "quarkus.minio.bucket-name", defaultValue = DEFAULT_BUCKET_NAME)
    String bucketName;

    @Inject
    Logger logger;

    @Inject
    MinioClient minioClient;

    public void onStartup(@Observes StartupEvent event) throws Exception {
        logger.info("Initializing File Storage ...");

        boolean bucketExists = minioClient.bucketExists(
            BucketExistsArgs.builder()
            .bucket(bucketName)
            .build());
        if (!bucketExists) {
            // Create bucket
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                .bucket(bucketName)
                .build());
            logger.infov("Bucket '{0}' was created.", bucketName);
        } else {
            logger.infov("Bucket '{0}' already exists.", bucketName);
        }
    }

    public String uploadFile(final InputStream inputStream,
        final String fileName, final MediaType contentType) {
        try {
            final String finalFileName = generateUniqueFileName(fileName);
            logger.infov("Uploading file {0} to bucket {1}", finalFileName, bucketName);
            minioClient.putObject(
                PutObjectArgs.builder()
                .bucket(bucketName)
                .object(finalFileName)
                .stream(inputStream, DEFAULT_OBJECT_SIZE, DEFAULT_PART_SIZE)
                .contentType(contentType.toString())
                .build());
            return finalFileName;
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while uploading file", e);
        }
    }

    public InputStream downloadFile(final String fileName) {
        try {
            logger.infov("Downloading file {0} from bucket {1}", fileName, bucketName);
            return minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .build());
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                logger.warnv("Object {0} does not exist in bucket {1}", fileName, bucketName);
                throw new NotFoundException(e);
            }
            throw new InternalServerErrorException("Error occurred while downloading file", e);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while downloading file", e);
        }
    }

    public void deleteFile(final String fileName) {
        try {
            logger.infov("Deleting file {0} from bucket {1}", fileName, bucketName);
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build());
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while deleting file", e);
        }
    }

    private boolean fileExists(final String fileName) {
        try {
            logger.infov("Checking if object {0} exists in bucket {1}", fileName, bucketName);
            minioClient.statObject(
                StatObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .build());
            logger.infov("Object {0} exists in bucket {1}", fileName, bucketName);
            return true;
        } catch (ErrorResponseException e) {
            if (e.response().code() == 404) {
                logger.infov("Object {0} does not exist in bucket {1}", fileName, bucketName);
                return false;
            }
            throw new InternalServerErrorException("Error occurred while checking if object exists", e);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while checking if object exists", e);
        }
    }

    private String generateUniqueFileName(final String fileName) {
        if (!fileExists(fileName)) {
            return fileName;
        }
        logger.infov("Generating unique file name for {0} in bucket {1}", fileName, bucketName);
        int dotIndex = fileName.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : fileName.substring(dotIndex);
        int counter = 1;
        String candidate;
        do {
            candidate = String.format("%s(%d)%s", baseName, counter++, extension);
        } while (fileExists(candidate));
        logger.infov("Generated unique file name: {0}", candidate);
        return candidate;
    }

}
