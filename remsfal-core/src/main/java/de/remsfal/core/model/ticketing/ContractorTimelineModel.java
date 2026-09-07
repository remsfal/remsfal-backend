package de.remsfal.core.model.ticketing;

import de.remsfal.core.model.UserContext;

import java.util.UUID;

public interface ContractorTimelineModel extends TimelineModel {

    UUID getOrganizationId();

    UserContext getSenderRole();

}
