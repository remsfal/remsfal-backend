package de.remsfal.core.model.ticketing;

import java.util.UUID;

/**
 * Represents an uploaded file in Minio.
 */
public interface FileUploadModel {

    UUID getSessionId();

    UUID getMessageId();

    UUID getSenderId();

    String getContentType();

    String getBucket();

    String getFileName();

}
