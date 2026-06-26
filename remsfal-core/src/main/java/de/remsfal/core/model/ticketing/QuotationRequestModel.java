package de.remsfal.core.model.ticketing;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface QuotationRequestModel {

    UUID getId();

    UUID getIssueId();

    UUID getProjectId();

    UUID getTriggerId();

    UUID getContractorId();

    @Nullable
    UUID getOrganizationId();

    @Nullable
    String getScopeOfWork();

    String getStatus();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
