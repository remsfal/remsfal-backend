package de.remsfal.core.model.ticketing;

import java.util.UUID;

public interface ContractorTimelineModel extends TimelineModel {

    UUID getRequestId();

    UUID getContractorId();

    UUID getOrganizationId();

    ParticipantRole getSenderRole();

}
