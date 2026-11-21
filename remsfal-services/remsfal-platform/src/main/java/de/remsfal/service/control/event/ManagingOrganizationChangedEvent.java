package de.remsfal.service.control.event;

import java.util.UUID;

/**
 * Synchronous CDI event fired by {@code ProjectController} whenever the managing organization of a project changes,
 * within the transaction that performs the change.
 * <p>
 * This is the case if a proprietor sets another managing organization, if the managing organization loses its role
 * PROPRIETOR or is removed from the project, and if the managing organization is deleted. An exception thrown by an
 * observer rolls back the change.
 *
 * @param projectId              id of the project
 * @param previousOrganizationId id of the previous managing organization, or {@code null}
 * @param newOrganizationId      id of the new managing organization, or {@code null} if the project has no managing
 *                               organization anymore
 * @param actingUserId           id of the user who triggered the change, or {@code null} if it is unknown
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public record ManagingOrganizationChangedEvent(UUID projectId, UUID previousOrganizationId, UUID newOrganizationId,
    UUID actingUserId) {
}
