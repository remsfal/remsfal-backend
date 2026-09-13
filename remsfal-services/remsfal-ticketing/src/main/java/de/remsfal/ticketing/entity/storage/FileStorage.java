package de.remsfal.ticketing.entity.storage;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;

import java.io.InputStream;

import org.jboss.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;

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

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class FileStorage {

    public static final String DEFAULT_BUCKET_NAME = "remsfal-ticketing";

    @ConfigProperty(name = "remsfal.ticketing.storage.bucket-name", defaultValue = DEFAULT_BUCKET_NAME)
    String bucketName;

    @Inject
    Logger logger;

    @Inject
    S3Client s3Client;

    public void onStartup(@Observes StartupEvent event) throws Exception {
        logger.info("Initializing File Storage ...");

        try {
            s3Client.headBucket(HeadBucketRequest.builder()
                .bucket(bucketName)
                .build());
            logger.infov("Bucket '{0}' already exists.", bucketName);
        } catch (NoSuchBucketException e) {
            createBucket();
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                createBucket();
            } else {
                throw e;
            }
        }
    }

    private void createBucket() {
        s3Client.createBucket(CreateBucketRequest.builder()
            .bucket(bucketName)
            .build());
        logger.infov("Bucket '{0}' was created.", bucketName);
    }

    public String uploadFile(final InputStream inputStream,
        final String fileName, final MediaType contentType) {
        try {
            final String finalFileName = generateUniqueFileName(fileName);
            logger.infov("Uploading file {0} to bucket {1}", finalFileName, bucketName);
            final byte[] bytes = inputStream.readAllBytes();
            s3Client.putObject(
                PutObjectRequest.builder()
                .bucket(bucketName)
                .key(finalFileName)
                .contentType(contentType.toString())
                .build(),
                RequestBody.fromBytes(bytes));
            return finalFileName;
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while uploading file", e);
        }
    }

    public InputStream downloadFile(final String fileName) {
        try {
            logger.infov("Downloading file {0} from bucket {1}", fileName, bucketName);
            return s3Client.getObject(
                GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build());
        } catch (NoSuchKeyException e) {
            logger.warnv("File {0} does not exist in bucket {1}", fileName, bucketName);
            throw new NotFoundException(e);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                logger.warnv("File {0} does not exist in bucket {1}", fileName, bucketName);
                throw new NotFoundException(e);
            }
            throw new InternalServerErrorException("Error occurred while downloading file", e);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while downloading object", e);
        }
    }

    public void deleteFile(final String fileName) {
        try {
            logger.infov("Deleting file {0} from bucket {1}", fileName, bucketName);
            s3Client.deleteObject(
                DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build());
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while deleting file", e);
        }
    }

    private boolean fileExists(final String fileName) {
        try {
            logger.debugv("Checking if file name {0} exists in bucket {1}", fileName, bucketName);
            s3Client.headObject(
                HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build());
            logger.infov("File name {0} already exists in bucket {1}", fileName, bucketName);
            return true;
        } catch (NoSuchKeyException e) {
            logger.debugv("File name {0} does not exist in bucket {1}", fileName, bucketName);
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                logger.debugv("File name {0} does not exist in bucket {1}", fileName, bucketName);
                return false;
            }
            throw new InternalServerErrorException("Error occurred while checking if file name exists", e);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while checking if object exists", e);
        }
    }

    private String generateUniqueFileName(final String fileName) {
        if (!fileExists(fileName)) {
            return fileName;
        }
        logger.debugv("Generating unique file name for {0} in bucket {1}", fileName, bucketName);
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
