package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.UUID;

/**
 * Model interface for a quotation response in the tendering process.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface QuotationModel {

    UUID getId();

    UUID getProjectId();

    UUID getIssueId();

    UUID getRequesterId();

    UUID getContractorId();

    String getText();

    Instant getCreatedAt();

    public enum QuotationStatus {
        VALID,
        INVALID
    }

    QuotationStatus getStatus();

}
