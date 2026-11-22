package de.remsfal.service.control.event;

import java.util.UUID;

import de.remsfal.core.json.organization.OrganizationJson;

/**
 * Extension point for changes of an organization, see {@link ProjectEvent} for the semantics.
 */
public sealed interface OrganizationEvent {

    /** Snapshot of the organization, taken within the transaction (for a deletion the last state). */
    OrganizationJson organization();

    UUID actingUserId();

    default UUID organizationId() {
        return organization().getId();
    }

    record Created(OrganizationJson organization, UUID actingUserId) implements OrganizationEvent {
    }

    /** Any change of the organization. */
    record Updated(OrganizationJson organization, UUID actingUserId) implements OrganizationEvent {
    }

    /**
     * The contact data of the organization (name, email, phone, trade or address) has changed, which is relevant for
     * the contractors linked to the organization. Fired in addition to {@link Updated}.
     */
    record ContactDataChanged(OrganizationJson organization, UUID actingUserId) implements OrganizationEvent {
    }

    record Deleted(OrganizationJson organization, UUID actingUserId) implements OrganizationEvent {
    }

}
