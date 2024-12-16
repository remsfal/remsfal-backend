package de.remsfal.service.control;

import io.minio.*;
import io.minio.messages.Item;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import io.minio.errors.MinioException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class FileStorageService {

    private static final String FILE_FORM_FIELD = "file";
    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    private static final String DEFAULT_FILE_NAME = "unknown";

    private final Set<String> allowedTypes = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "text/plain",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/json"
    );

    @ConfigProperty(name = "%dev.quarkus.minio.url")
    String endpoint;

    @Inject
    Logger logger;

    @Inject
    MinioClient minioClient;

    public String uploadFile(String bucketName, MultipartFormDataInput input) throws Exception {
        List<InputPart> inputParts = getFileInputParts(input);

        if (inputParts == null || inputParts.isEmpty()) {
            logger.error("File is null or empty");
            throw new MinioException("File is null or empty");
        }

        InputPart inputPart = inputParts.get(0);
        String originalFileName = extractFileName(inputPart.getHeaders());
        String contentType = inputPart.getMediaType().toString();

        if (!isValidContentType(contentType)) {
            logger.error("Invalid file type: " + contentType);
            throw new MinioException("Invalid file type: " + contentType);
        }

        try (InputStream inputStream = inputPart.getBody(InputStream.class, null)) {
            ensureBucketExists(bucketName);
            String finalFileName = generateUniqueFileName(bucketName, originalFileName);

            logger.infov("Uploading file {0} to bucket {1} as {2}", originalFileName, bucketName, finalFileName);
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(finalFileName)
                            .stream(inputStream, -1, 10485760)
                            .contentType(contentType)
                            .build()
            );

            String fileUrl = constructFileUrl(bucketName, finalFileName);
            logger.infov("File uploaded successfully. URL: {0}", fileUrl);
            return fileUrl;

        } catch (MinioException e) {
            logger.error("Error uploading file to Minio", e);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to process uploaded file", e);
            throw e;
        }
    }

    public InputStream downloadFile(String bucketName, String objectName) throws Exception {
        logger.infov("Downloading file {0} from bucket {1}", objectName, bucketName);

        if (!checkBucketExists(bucketName)) {
            throw new MinioException("Bucket does not exist");
        }

        if (!objectExists(bucketName, objectName)) {
            logger.errorv("File {0} does not exist in bucket {1}", objectName, bucketName);
            throw new MinioException("File does not exist");
        }

        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            InputStream stream = minioClient.getObject(args);
            logger.infov("File {0} from bucket {1} downloaded successfully!", objectName, bucketName);
            return stream;

        } catch (MinioException e) {
            logger.error("Error downloading file from Minio", e);
            throw e;
        }
    }

    public String getContentType(String bucketName, String objectName) throws Exception {
        logger.infov("Retrieving metadata for file {0} in bucket {1}", objectName, bucketName);

        if (!objectExists(bucketName, objectName)) {
            throw new MinioException("File does not exist");
        }

        if (!checkBucketExists(bucketName)) {
            throw new MinioException("Bucket does not exist");
        }

        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            StatObjectResponse stat = minioClient.statObject(args);
            return stat.contentType();

        } catch (MinioException e) {
            throw new Exception("Error occurred while retrieving file metadata", e);
        }
    }

    public Iterable<Result<Item>> listObjects(String bucketName) throws Exception {
        if (!checkBucketExists(bucketName)) {
            throw new MinioException("Bucket does not exist");
        }

        try {
            logger.infov("Listing objects in bucket {0}", bucketName);
            return minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName).build());
        } catch (Exception e) {
            logger.error("Error listing objects in bucket", e);
            throw e;
        }
    }

    public void deleteObject(String bucketName, String objectName) throws Exception {
        if (!objectExists(bucketName, objectName)) {
            logger.infov("Object {0} does not exist in bucket {1}", objectName, bucketName);
            throw new MinioException("File does not exist");
        }

        if (!checkBucketExists(bucketName)) {
            throw new MinioException("Bucket does not exist");
        }

        logger.infov("Deleting object {0} from bucket {1}", objectName, bucketName);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Not found")) {
                throw new MinioException("File does not exist");
            }
            throw e;
        }
    }

    private boolean checkBucketExists(String bucketName) throws Exception {
        logger.infov("Checking if bucket {0} exists", bucketName);
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        logger.infov("Bucket {0} exists: {1}", bucketName, exists);
        return exists;
    }

    private void ensureBucketExists(String bucketName) throws Exception {
        logger.infov("Ensuring bucket {0} exists", bucketName);
        if (!checkBucketExists(bucketName)) {
            logger.infov("No bucket named {0} was found! Creating one", bucketName);
            createBucket(bucketName);
        }
    }

    private void createBucket(String bucketName) throws Exception {
        logger.infov("Creating bucket {0}", bucketName);
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }

    private boolean objectExists(String bucketName, String objectName) throws MinioException {
        logger.infov("Checking if object {0} exists in bucket {1}", objectName, bucketName);
        try {
            minioClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
            logger.infov("Object {0} exists in bucket {1}", objectName, bucketName);
            return true;
        } catch (io.minio.errors.ErrorResponseException e) {
            if (e.response().code() == 404) {
                logger.infov("Object {0} does not exist in bucket {1}", objectName, bucketName);
                return false;
            }
            logger.error("Error checking if object exists", e);
            throw new MinioException("Error checking if object exists: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while checking if object exists", e);
            throw new MinioException("Unexpected error while checking if object exists: " + e.getMessage());
        }
    }

    private String extractFileName(Map<String, List<String>> headers) {
        logger.infov("Retrieving file name from headers: {0}", headers);
        List<String> contentDispositionList = headers.get(CONTENT_DISPOSITION_HEADER);

        if (contentDispositionList == null || contentDispositionList.isEmpty()) {
            logger.warn("Content-Disposition header is missing");
            return DEFAULT_FILE_NAME;
        }

        String contentDisposition = contentDispositionList.get(0);
        for (String part : contentDisposition.split(";")) {
            part = part.trim();
            if (part.startsWith("filename")) {
                String[] nameParts = part.split("=");
                if (nameParts.length > 1) {
                    String fileName = nameParts[1].trim().replaceAll("\"", "");
                    logger.infov("Extracted file name: {0}", fileName);
                    return fileName;
                }
            }
        }

        logger.warn("Filename not found in Content-Disposition header, using default name 'unknown'");
        return DEFAULT_FILE_NAME;
    }

    private boolean isValidContentType(String contentType) {
        logger.infov("Checking if content type {0} is valid", contentType);
        boolean isValid = allowedTypes.contains(contentType);
        if (!isValid) {
            logger.warnv("Content type {0} is not allowed", contentType);
        }
        return isValid;
    }

    private List<InputPart> getFileInputParts(MultipartFormDataInput input) {
        logger.infov("Retrieving file input parts: {0}", input);
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        List<InputPart> fileParts = uploadForm.get(FILE_FORM_FIELD);
        if (fileParts == null || fileParts.isEmpty()) {
            logger.warn("No 'file' part found in the form data");
        }
        return fileParts;
    }

    private String constructFileUrl(String bucketName, String fileName) {
        String fileUrl = endpoint.endsWith("/") ? endpoint : endpoint + "/";
        fileUrl += bucketName + "/" + fileName;
        logger.infov("Constructed file URL: {0}", fileUrl);
        return fileUrl;
    }

    private String generateUniqueFileName(String bucketName, String fileName) throws Exception {
        logger.infov("Generating unique file name for '{0}' in bucket '{1}'", fileName, bucketName);

        if (!objectExists(bucketName, fileName)) {
            return fileName;
        }

        int dotIndex = fileName.lastIndexOf('.');
        String baseName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        String extension = (dotIndex == -1) ? "" : fileName.substring(dotIndex);
        int counter = 1;
        String candidate;

        do {
            candidate = String.format("%s(%d)%s", baseName, counter++, extension);
        } while (objectExists(bucketName, candidate));

        logger.infov("Generated unique file name: {0}", candidate);
        return candidate;
    }
}
