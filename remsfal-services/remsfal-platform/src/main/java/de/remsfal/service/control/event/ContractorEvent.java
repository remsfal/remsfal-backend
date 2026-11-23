package de.remsfal.service.control.event;

import java.util.UUID;

import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.UserJson;

/**
 * Extension point for changes of the contractors of a project, see {@link ProjectEvent} for the semantics.
 */
public sealed interface ContractorEvent {

    UUID projectId();

    /** Snapshot of the contractor, taken within the transaction (for a deletion the last state). */
    ContractorJson contractor();

    UserJson actingUser();

    record Created(UUID projectId, ContractorJson contractor, UserJson actingUser) implements ContractorEvent {
    }

    record Updated(UUID projectId, ContractorJson contractor, UserJson actingUser) implements ContractorEvent {
    }

    record Deleted(UUID projectId, ContractorJson contractor, UserJson actingUser) implements ContractorEvent {
    }

}
