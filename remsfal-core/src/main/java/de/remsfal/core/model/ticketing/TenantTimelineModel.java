package de.remsfal.core.model.ticketing;

import de.remsfal.core.model.UserContext;

public interface TenantTimelineModel extends TimelineModel {

    UserContext getSenderRole();

}
