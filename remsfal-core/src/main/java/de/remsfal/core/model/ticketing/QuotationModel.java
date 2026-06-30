package de.remsfal.core.model.ticketing;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface QuotationModel {

    enum QuotationStatus {
        VALID,
        INVALID,
        ACCEPTED,
        REJECTED
    }

    UUID getId();

    UUID getIssueId();

    UUID getRequestId();

    UUID getProjectId();

    UUID getOffererId();

    String getOfferedBy();

    UUID getContractorId();

    @Nullable
    List<UUID> getAttachments();

    @Nullable
    Instant getValidUntil();

    QuotationStatus getStatus();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
