package de.remsfal.core.model.notification;

import java.util.UUID;

/**
 * Email preferences of a user. Each flag decides whether the user receives the respective
 * issue email. Mandatory emails (e.g. registration or project admission) are not configurable.
 */
public interface EmailSettingsModel {

    UUID getUserId();

    Boolean isIssueCreated();

    Boolean isIssueUpdated();

    Boolean isIssueAssigned();

    Boolean isIssueMessageCreated();

}
