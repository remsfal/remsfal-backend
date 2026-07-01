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

    UUID getOrdererId();

    String getOrderedBy();

    UUID getContractorId();

    @Nullable
    String getContractorName();

    UUID getOrganizationId();

    OrderPlacementStatus getStatus();

    @Nullable
    UUID getConfirmorId();

    @Nullable
    String getConfirmedBy();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
