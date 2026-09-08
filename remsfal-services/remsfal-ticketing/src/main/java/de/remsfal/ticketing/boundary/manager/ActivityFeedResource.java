package de.remsfal.ticketing.boundary.manager;

import de.remsfal.core.api.ticketing.manager.ActivityFeedEndpoint;
import de.remsfal.core.json.ticketing.ActivityFeedJson;
import de.remsfal.core.json.ticketing.ActivityFeedListJson;
import de.remsfal.ticketing.boundary.AbstractTicketingResource;
import de.remsfal.ticketing.control.ActivityFeedController;
import de.remsfal.ticketing.entity.dto.ActivityFeedEntity;
import de.remsfal.ticketing.entity.filter.ActivityFeedFilter;

import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class ActivityFeedResource extends AbstractTicketingResource implements ActivityFeedEndpoint {

    @Inject
    ActivityFeedController controller;

    @Override
    public ActivityFeedListJson getActivities(final UUID projectId, final UUID issueId, final UUID agreementId,
        final UUID organizationId, final UUID contractorId, final UUID assigneeId,
        final UUID cursor, final Integer limit) {
        final ActivityFeedFilter filter = new ActivityFeedFilter(projectId, issueId, agreementId,
            organizationId, contractorId, assigneeId);
        final List<ActivityFeedEntity> activities =
            controller.getActivities(principal.getId(), filter, cursor, limit);
        return ActivityFeedListJson.valueOf(activities, nextActivityCursorOf(activities, limit));
    }

    @Override
    public ActivityFeedJson updateActivityStatus(final UUID activityId, final Boolean read) {
        try {
            final ActivityFeedEntity updated = controller.updateActivityStatus(principal.getId(), activityId, read);
            return ActivityFeedJson.valueOf(updated);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @Override
    public void deleteActivity(final UUID activityId) {
        try {
            controller.deleteActivity(principal.getId(), activityId);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

}
