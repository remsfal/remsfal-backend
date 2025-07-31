package de.remsfal.core.model.ticketing;

/**
 * Represents an uploaded file in Minio.
 */
public interface FileUploadModel {

    String getSessionId();

    String getMessageId();

    String getSenderId();

    String getMediaType();

    String getBucket();

    String getFileName();

}
