package de.remsfal.service.control.event;

import java.util.UUID;

import de.remsfal.core.json.project.RentalAgreementJson;

/**
 * Extension point for changes of the rental agreements of a project, see {@link ProjectEvent} for the semantics.
 * Changes of the tenants or rents of an agreement are reported as {@link Updated}.
 */
public sealed interface RentalAgreementEvent {

    UUID projectId();

    /** Snapshot of the rental agreement, taken within the transaction (for a deletion the last state). */
    RentalAgreementJson agreement();

    UUID actingUserId();

    record Created(UUID projectId, RentalAgreementJson agreement, UUID actingUserId) implements RentalAgreementEvent {
    }

    record Updated(UUID projectId, RentalAgreementJson agreement, UUID actingUserId) implements RentalAgreementEvent {
    }

    record Deleted(UUID projectId, RentalAgreementJson agreement, UUID actingUserId) implements RentalAgreementEvent {
    }

}
