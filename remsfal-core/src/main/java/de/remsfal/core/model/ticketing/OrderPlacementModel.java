package de.remsfal.core.model.ticketing;

import jakarta.annotation.Nullable;

import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface OrderPlacementModel extends OrderProcessModel {

    enum OrderPlacementStatus {
        PLACED,
        CONFIRMED,
        REJECTED,
        WITHDRAWN
    }

    UUID getQuotationId();

    UUID getOrdererId();

    String getOrderedBy();

    UUID getOrganizationId();

    OrderPlacementStatus getStatus();

    @Nullable
    UUID getConfirmorId();

    @Nullable
    String getConfirmedBy();

}
