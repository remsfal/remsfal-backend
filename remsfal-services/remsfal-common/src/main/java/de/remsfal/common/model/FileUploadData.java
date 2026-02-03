package de.remsfal.common.model;

import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;

/**
 * Transport-neutral wrapper for file upload data.
 * Used to pass file data from boundary layer to control layer without depending on transport-specific classes.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public class FileUploadData {

    private final InputStream inputStream;
    private final String fileName;
    private final MediaType mediaType;

    public FileUploadData(final InputStream inputStream, final String fileName, final MediaType mediaType) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("File name cannot be null or blank");
        }
        if (mediaType == null) {
            throw new IllegalArgumentException("Media type cannot be null");
        }
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.mediaType = mediaType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public MediaType getMediaType() {
        return mediaType;
    }
}
