package de.remsfal.service.control.event;

import java.util.UUID;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.json.project.TenantJson;

/**
 * Extension point for changes of the tenants of a project, see {@link ProjectEvent} for the semantics.
 */
public sealed interface TenantEvent {

    UUID projectId();

    /** Snapshot of the tenant, taken within the transaction (for a deletion the last state). */
    TenantJson tenant();

    UserJson actingUser();

    record Created(UUID projectId, TenantJson tenant, UserJson actingUser) implements TenantEvent {
    }

    record Updated(UUID projectId, TenantJson tenant, UserJson actingUser) implements TenantEvent {
    }

    record Deleted(UUID projectId, TenantJson tenant, UserJson actingUser) implements TenantEvent {
    }

}
