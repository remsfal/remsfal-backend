package de.remsfal.ticketing.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;

import de.remsfal.common.model.FileUploadData;
import de.remsfal.common.validation.MediaTypeValidator;
import de.remsfal.ticketing.entity.storage.FileStorage;

import java.io.InputStream;

import org.jboss.logging.Logger;

/**
 * Controller for handling file storage operations such as upload, download, and delete.
 * All methods of this class are package-private and intended to be used only by other controllers.
 *
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@ApplicationScoped
public class FileStorageController {

    @Inject
    Logger logger;

    @Inject
    FileStorage storage;

    /**
     * Uploads a file to storage.
     *
     * @param fileData the file upload data wrapper (must not be null)
     * @param fileName the target file name (must not be null or blank)
     * @return the final file name or identifier of the uploaded file
     * @throws BadRequestException if the content type is invalid, or if fileData or fileName are invalid
     */
    String uploadFile(final FileUploadData fileData, final String fileName) {
        if (fileData == null) {
            logger.error("FileUploadData is null");
            throw new BadRequestException("FileUploadData cannot be null");
        }
        return uploadFile(fileData.getInputStream(), fileName, fileData.getMediaType());
    }

    /**
     * Uploads a file to storage.
     *
     * @param inputStream the file content as an input stream (must not be null)
     * @param fileName the original file name (must not be null or blank)
     * @param contentType the media type of the file (must not be null)
     * @return the final file name or identifier of the uploaded file
     * @throws BadRequestException if the content type is invalid, or if inputStream or fileName are invalid
     */
    String uploadFile(final InputStream inputStream, final String fileName, final MediaType contentType) {
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
        if (!isContentTypeValid(contentType)) {
            logger.error("Invalid file type: " + contentType);
            throw new BadRequestException("Invalid file type: " + contentType.toString());
        }
        logger.infov("Uploading file: {0} with content type: {1}", fileName, contentType);
        return storage.uploadFile(inputStream, fileName, contentType);
    }

    /**
     * Downloads a file from storage.
     *
     * @param fileName the name or identifier of the file to download
     * @return an InputStream of the file content
     */
    InputStream downloadFile(final String fileName) {
        return storage.downloadFile(fileName);
    }

    /**
     * Deletes a file from storage.
     *
     * @param fileName the name or identifier of the file to delete
     */
    void deleteFile(final String fileName) {
        storage.deleteFile(fileName);
    }

    /**
     * Validates whether the given content type is allowed for file uploads.
     *
     * @param contentType the content type to validate
     * @return true if the content type is allowed, false otherwise
     */
    boolean isContentTypeValid(final MediaType contentType) {
        logger.debugv("Checking if content type {0} is valid", contentType);
        return MediaTypeValidator.isValid(contentType);
    }

}
