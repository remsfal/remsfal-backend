package de.remsfal.core.model.ticketing;

import java.util.UUID;

/**
 * Represents an attachment associated with an issue.
 *
 * @author GitHub Copilot
 */
public interface IssueAttachmentModel {

    UUID getIssueId();

    UUID getAttachmentId();

    String getFileName();

    String getContentType();

    String getBucket();

    String getObjectName();

    Long getFileSize();

    UUID getUploadedBy();

}
