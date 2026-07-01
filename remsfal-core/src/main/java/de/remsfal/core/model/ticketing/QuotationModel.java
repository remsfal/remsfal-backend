package de.remsfal.core.model.ticketing;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface QuotationModel extends OrderProcessModel {

    enum QuotationStatus {
        VALID,
        INVALID,
        ACCEPTED,
        REJECTED
    }

    UUID getRequestId();

    UUID getOffererId();

    String getOfferedBy();

    QuotationStatus getStatus();

    @Nullable
    Instant getValidUntil();

}
