package de.remsfal.ticketing.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;

import de.remsfal.ticketing.entity.storage.FileStorage;

import java.io.InputStream;
import java.util.Set;

import org.jboss.logging.Logger;

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@ApplicationScoped
public class FileStorageController {

    private final Set<String> allowedTypes = Set.of(
        "image/jpg",
        "image/jpeg",
        "image/png",
        "image/gif",
        "text/plain",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/json");

    @Inject
    Logger logger;

    @Inject
    FileStorage storage;

    /**
     * Uploads a file to storage.
     *
     * @param inputStream the file content as an input stream (must not be null)
     * @param fileName the original file name (must not be null or blank)
     * @param contentType the media type of the file (must not be null)
     * @return the URL or identifier of the uploaded file
     * @throws BadRequestException if the content type is invalid, or if inputStream or fileName are invalid
     */
    public String uploadFile(final InputStream inputStream, final String fileName, final MediaType contentType) {
        if (inputStream == null) {
            logger.error("Input stream is null");
            throw new BadRequestException("Input stream cannot be null");
        }
        if (fileName == null || fileName.isBlank()) {
            logger.error("File name is null or blank");
            throw new BadRequestException("File name cannot be null or blank");
        }
        if (contentType == null) {
            logger.error("Content type is null");
            throw new BadRequestException("Content type cannot be null");
        }
        if (!isValidContentType(contentType.toString())) {
            logger.error("Invalid file type: " + contentType);
            throw new BadRequestException("Invalid file type: " + contentType.toString());
        }
        logger.infov("Uploading file: {0} with content type: {1}", fileName, contentType);
        return storage.uploadFile(inputStream, fileName, contentType);
    }

    public InputStream downloadFile(final String objectName) {
        return storage.downloadFile(objectName);
    }

    public void deleteFile(final String fileName) {
        storage.deleteFile(fileName);
    }

    /**
     * Validates whether the given content type is allowed for file uploads.
     * The content type is normalized by removing any parameters (e.g., charset)
     * before checking against the allowed types set.
     *
     * @param contentType the content type to validate
     * @return true if the content type is allowed, false otherwise
     */
    public boolean isValidContentType(String contentType) {
        logger.infov("Checking if content type {0} is valid", contentType);
        // Normalize the content type to remove parameters (e.g., charset=UTF-8)
        String normalizedContentType = contentType.split(";")[0].trim();
        boolean isValid = allowedTypes.contains(normalizedContentType);
        if (!isValid) {
            logger.warnv("Content type {0} is not allowed", contentType);
        }
        return isValid;
    }

    public Set<String> getAllowedTypes() {
        return allowedTypes;
    }
}
