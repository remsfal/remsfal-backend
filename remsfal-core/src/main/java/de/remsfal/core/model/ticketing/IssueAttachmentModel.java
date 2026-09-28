package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.UUID;

import de.remsfal.core.model.UserContext;

/**
 * Represents an attachment associated with an issue. Every file of an issue is stored exactly once;
 * who may see it is decided by the timelines and order processes that reference its id.
 */
public interface IssueAttachmentModel {

    UUID getIssueId();

    UUID getAttachmentId();

    String getFileName();

    String getContentType();

    String getObjectName();

    UUID getUploaderId();

    String getUploadedBy();

    UserContext getUploaderContext();

    Instant getCreatedAt();

}
