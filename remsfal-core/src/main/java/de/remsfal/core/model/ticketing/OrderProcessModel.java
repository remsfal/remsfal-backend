package de.remsfal.core.model.ticketing;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface OrderProcessModel {

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

    UUID getContractorId();

    @Nullable
    String getContractorName();

    @Nullable
    UUID getOrganizationId();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
