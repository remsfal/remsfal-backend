package de.remsfal.core.model.ticketing;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface QuotationRequestModel {

    enum RequestStatus {
        REQUESTED,
        WITHDRAWN,
        VIEWING_REQUIRED,
        CONSULTATION_REQUIRED,
        REJECTED,
        SUBMITTED
    }

    UUID getId();

    UUID getIssueId();

    UUID getProjectId();

    UUID getTriggerId();

    UUID getContractorId();

    @Nullable
    UUID getOrganizationId();

    @Nullable
    String getContractorName();

    @Nullable
    String getScopeOfWork();

    RequestStatus getStatus();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
