package de.remsfal.core.model.ticketing;

import jakarta.annotation.Nullable;

import java.time.Instant;
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

    @Nullable
    String getProjectOwner();

    @Nullable
    String getProjectCareOf();

    @Nullable
    String getProjectBillingAddress1();

    @Nullable
    String getProjectBillingAddress2();

    @Nullable
    String getProjectBillingAddress3();

    UUID getOffererId();

    String getOfferedBy();

    UUID getContractorId();

    @Nullable
    String getContractorName();

    @Nullable
    UUID getOrganizationId();

    QuotationStatus getStatus();

    @Nullable
    Instant getValidUntil();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
