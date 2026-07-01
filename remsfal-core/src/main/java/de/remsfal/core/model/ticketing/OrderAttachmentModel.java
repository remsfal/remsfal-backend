package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents an attachment associated with a quotation request, quotation, or order placement.
 */
public interface OrderAttachmentModel {

    OrderProcessPhase getProcessPhase();

    UUID getProcessId();

    UUID getAttachmentId();

    String getFileName();

    String getContentType();

    String getObjectName();

    UUID getUploaderId();

    String getUploadedBy();

    Instant getCreatedAt();

}
