package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an attachment associated with an issue.
 */
public interface IssueAttachmentModel {

    UUID getIssueId();

    UUID getAttachmentId();

    String getFileName();

    String getContentType();

    String getObjectName();

    UUID getUploadedBy();

    Instant getCreatedAt();

}
