package de.remsfal.core.model.ticketing;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface OrderPlacementModel {

    enum OrderPlacementStatus {
        PLACED,
        CONFIRMED,
        REJECTED,
        WITHDRAWN
    }

    UUID getId();

    UUID getIssueId();

    UUID getQuotationId();

    UUID getProjectId();

    UUID getOrdererId();

    String getOrderedBy();

    UUID getContractorId();

    UUID getOrganizationId();

    @Nullable
    UUID getConfirmorId();

    @Nullable
    String getConfirmedBy();

    OrderPlacementStatus getStatus();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
