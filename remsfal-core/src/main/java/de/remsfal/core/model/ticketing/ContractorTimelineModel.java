package de.remsfal.core.model.ticketing;

import java.util.UUID;

public interface ContractorTimelineModel extends TimelineModel {

    UUID getRequestId();

    ParticipantRole getSenderRole();

    ParticipantRole getRecipient();

}
