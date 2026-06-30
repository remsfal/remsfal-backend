package de.remsfal.core.model.ticketing;

import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface OrderPlacementModel {

    enum OrderPlacementStatus {
        PLACED("placed"),
        CONFIRMED("confirmed"),
        REJECTED("rejected"),
        WITHDRAWN("withdrawn");

        private final String value;

        OrderPlacementStatus(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static OrderPlacementStatus valueOfStatus(final String value) {
            return Arrays.stream(values())
                .filter(status -> status.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown order placement status: " + value));
        }
    }

    UUID getId();

    UUID getIssueId();

    UUID getQuotationId();

    UUID getProjectId();

    UUID getOrdererId();

    String getOrderedBy();

    UUID getContractorId();

    @Nullable
    UUID getConfirmorId();

    @Nullable
    String getConfirmedBy();

    OrderPlacementStatus getStatus();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
