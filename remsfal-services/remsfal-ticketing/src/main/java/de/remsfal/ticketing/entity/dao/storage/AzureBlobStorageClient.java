package de.remsfal.ticketing.entity.dao.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobErrorCode;
import com.azure.storage.blob.models.BlobStorageException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.io.InputStream;

/**
 * Azure Blob Storage implementation of StorageClient.
 */
public class AzureBlobStorageClient implements StorageClient {

    private final BlobServiceClient blobServiceClient;
    private final String containerName;
    private final Logger logger;
    private BlobContainerClient containerClient;

    public AzureBlobStorageClient(String connectionString, String containerName, Logger logger) {
        this.blobServiceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        this.containerName = containerName;
        this.logger = logger;
    }

    @Override
    public void initialize() throws Exception {
        logger.info("Initializing Azure Blob Storage client...");
        containerClient = blobServiceClient.getBlobContainerClient(containerName);
        
        if (!containerClient.exists()) {
            containerClient = blobServiceClient.createBlobContainer(containerName);
            logger.infov("Azure container '{0}' was created.", containerName);
        } else {
            logger.infov("Azure container '{0}' already exists.", containerName);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String fileName, MediaType contentType) throws Exception {
        logger.infov("Uploading file {0} to Azure container {1}", fileName, containerName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        blobClient.upload(inputStream, inputStream.available(), true);
        
        // Set content type
        if (contentType != null) {
            blobClient.setHttpHeaders(
                new com.azure.storage.blob.models.BlobHttpHeaders()
                    .setContentType(contentType.toString())
            );
        }
        
        return fileName;
    }

    @Override
    public InputStream downloadFile(String fileName) throws Exception {
        try {
            logger.infov("Downloading file {0} from Azure container {1}", fileName, containerName);
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            return blobClient.openInputStream();
        } catch (BlobStorageException e) {
            if (e.getErrorCode() == BlobErrorCode.BLOB_NOT_FOUND) {
                logger.warnv("Blob {0} does not exist in container {1}", fileName, containerName);
                throw new NotFoundException(e);
            }
            throw e;
        }
    }

    @Override
    public void deleteFile(String fileName) throws Exception {
        logger.infov("Deleting file {0} from Azure container {1}", fileName, containerName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        blobClient.delete();
    }

    @Override
    public boolean fileExists(String fileName) throws Exception {
        logger.debugv("Checking if blob {0} exists in Azure container {1}", fileName, containerName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        return blobClient.exists();
    }
}
