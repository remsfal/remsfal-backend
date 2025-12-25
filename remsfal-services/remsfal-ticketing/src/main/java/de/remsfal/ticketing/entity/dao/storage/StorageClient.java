package de.remsfal.ticketing.entity.dao.storage;

import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * Abstraction for object storage operations.
 * Implementations can use MinIO, Azure Blob Storage, or other storage backends.
 */
public interface StorageClient {

    /**
     * Initialize the storage client and ensure the bucket/container exists.
     * @throws Exception if initialization fails
     */
    void initialize() throws Exception;

    /**
     * Upload a file to storage.
     * @param inputStream the file content
     * @param fileName the file name
     * @param contentType the content type
     * @return the final file name (may be modified for uniqueness)
     * @throws Exception if upload fails
     */
    String uploadFile(InputStream inputStream, String fileName, MediaType contentType) throws Exception;

    /**
     * Download a file from storage.
     * @param fileName the file name
     * @return input stream of the file content
     * @throws Exception if download fails
     */
    InputStream downloadFile(String fileName) throws Exception;

    /**
     * Delete a file from storage.
     * @param fileName the file name
     * @throws Exception if deletion fails
     */
    void deleteFile(String fileName) throws Exception;

    /**
     * Check if a file exists in storage.
     * @param fileName the file name
     * @return true if file exists, false otherwise
     * @throws Exception if check fails
     */
    boolean fileExists(String fileName) throws Exception;
}
