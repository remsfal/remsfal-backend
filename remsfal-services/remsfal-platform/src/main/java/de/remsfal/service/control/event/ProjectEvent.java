package de.remsfal.service.control.event;

import java.util.UUID;

import de.remsfal.core.json.project.ProjectJson;

/**
 * Extension point for changes of a project. Fired synchronously within the transaction of the change: observers can
 * reject the change by throwing an exception (rollback). Observers with side effects outside the database observe
 * with {@code during = TransactionPhase.AFTER_SUCCESS}.
 */
public sealed interface ProjectEvent {

    /** Snapshot of the project, taken within the transaction (for a deletion the last state). */
    ProjectJson project();

    /** The user who triggered the change, null if the change was not triggered by a user. */
    UUID actingUserId();

    default UUID projectId() {
        return project().getId();
    }

    record Created(ProjectJson project, UUID actingUserId) implements ProjectEvent {
    }

    record Updated(ProjectJson project, UUID actingUserId) implements ProjectEvent {
    }

    record Deleted(ProjectJson project, UUID actingUserId) implements ProjectEvent {
    }

    /**
     * The managing organization of the project has changed. The new managing organization (or none) is part of the
     * project snapshot.
     */
    record ManagingOrganizationChanged(ProjectJson project, UUID previousOrganizationId, UUID actingUserId)
        implements ProjectEvent {
    }

}
