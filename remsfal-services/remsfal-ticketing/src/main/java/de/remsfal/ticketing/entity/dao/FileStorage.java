package de.remsfal.ticketing.entity.dao;

import de.remsfal.ticketing.entity.dao.storage.AzureBlobStorageClient;
import de.remsfal.ticketing.entity.dao.storage.MinioStorageClient;
import de.remsfal.ticketing.entity.dao.storage.StorageClient;
import io.minio.MinioClient;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.InputStream;
import java.util.Optional;

/**
 * File storage service supporting multiple backends (MinIO, Azure Blob Storage).
 * The implementation is selected at runtime based on configuration.
 */
@ApplicationScoped
public class FileStorage {

    public static final String DEFAULT_BUCKET_NAME = "remsfal-ticketing";
    private static final String STORAGE_PROVIDER_MINIO = "minio";
    private static final String STORAGE_PROVIDER_AZURE = "azure";

    @ConfigProperty(name = "storage.provider", defaultValue = STORAGE_PROVIDER_MINIO)
    String storageProvider;

    @ConfigProperty(name = "storage.bucket-name", defaultValue = DEFAULT_BUCKET_NAME)
    String bucketName;

    // Azure configuration
    @ConfigProperty(name = "azure.storage.connection-string")
    Optional<String> azureConnectionString;

    @Inject
    Logger logger;

    // MinIO client - optional injection (only available when MinIO extension is enabled)
    @Inject
    Instance<MinioClient> minioClientInstance;

    private StorageClient storageClient;

    public void onStartup(@Observes StartupEvent event) throws Exception {
        logger.infov("Initializing File Storage with provider: {0}", storageProvider);

        switch (storageProvider.toLowerCase()) {
            case STORAGE_PROVIDER_MINIO:
                if (!minioClientInstance.isResolvable()) {
                    throw new IllegalStateException("MinIO client not available but provider is set to 'minio'");
                }
                storageClient = new MinioStorageClient(minioClientInstance.get(), bucketName, logger);
                break;
            case STORAGE_PROVIDER_AZURE:
                if (azureConnectionString.isEmpty()) {
                    throw new IllegalStateException("Azure connection string not configured but provider is set to 'azure'");
                }
                storageClient = new AzureBlobStorageClient(azureConnectionString.get(), bucketName, logger);
                break;
            default:
                throw new IllegalArgumentException("Unknown storage provider: " + storageProvider);
        }

        storageClient.initialize();
        logger.infov("File Storage initialized successfully with {0}", storageProvider);
    }

    public String uploadFile(final InputStream inputStream,
        final String fileName, final MediaType contentType) {
        try {
            final String finalFileName = generateUniqueFileName(fileName);
            return storageClient.uploadFile(inputStream, finalFileName, contentType);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while uploading file", e);
        }
    }

    public InputStream downloadFile(final String fileName) {
        try {
            return storageClient.downloadFile(fileName);
        } catch (jakarta.ws.rs.NotFoundException e) {
            throw e; // Re-throw NotFoundException as-is
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while downloading file", e);
        }
    }

    public void deleteFile(final String fileName) {
        try {
            storageClient.deleteFile(fileName);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error occurred while deleting file", e);
        }
    }

    private boolean fileExists(final String fileName) {
        try {
            return storageClient.fileExists(fileName);
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
