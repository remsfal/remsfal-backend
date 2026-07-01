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

    UUID getInitiatorId();

    String getInitiatedBy();

    UUID getContractorId();

    @Nullable
    String getContractorName();

    @Nullable
    UUID getOrganizationId();

    RequestStatus getStatus();

    @Nullable
    String getScopeOfWork();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
