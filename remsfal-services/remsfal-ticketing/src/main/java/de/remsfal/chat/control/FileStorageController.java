package de.remsfal.chat.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import de.remsfal.chat.entity.dao.FileStorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

/**
 * @author Parham Rahmani [parham.rahmani@student.htw-berlin.de]
 */
@ApplicationScoped
public class FileStorageController {

    private static final String FILE_FORM_FIELD = "file";
    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    private static final String DEFAULT_FILE_NAME = "unknown";

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

    public String uploadFile(final MultipartFormDataInput input) {
        List<InputPart> inputParts = getFileInputParts(input);
        if (inputParts == null || inputParts.isEmpty()) {
            logger.error("File is null or empty");
            throw new BadRequestException("File is null or empty");
        }
        InputPart inputPart = inputParts.get(0);
        String originalFileName = extractFileName(inputPart.getHeaders());
        MediaType contentType = inputPart.getMediaType();
        if (!isValidContentType(contentType.toString())) {
            logger.error("Invalid file type: " + contentType);
            throw new BadRequestException("Invalid file type: " + contentType.toString());
        }
        try (InputStream inputStream = inputPart.getBody(InputStream.class, null)) {
            return storage.uploadFile(inputStream, originalFileName, contentType);
        } catch (IOException e) {
            throw new BadRequestException("Invalid input stream", e);
        }
    }

    public InputStream downloadFile(final String objectName) {
        return storage.downloadFile(objectName);
    }

    public void deleteFile(final String fileName) {
        storage.deleteFile(fileName);
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
        // Normalize the content type to remove parameters (e.g., charset=UTF-8)
        String normalizedContentType = contentType.split(";")[0].trim();
        boolean isValid = allowedTypes.contains(normalizedContentType);
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

    public Set<String> getAllowedTypes() {
        return allowedTypes;
    }
}
