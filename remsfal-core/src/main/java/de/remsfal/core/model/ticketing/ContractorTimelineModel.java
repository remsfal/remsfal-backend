package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ContractorTimelineModel {

    UUID getRequestId();

    UUID getIssueId();

    UUID getTimelineId();

    List<UUID> getAttachmentIds();

    UUID getSenderId();

    String getSenderName();

    ParticipantRole getSenderRole();

    ParticipantRole getRecipient();

    String getTitle();

    MessagePurpose getPurpose();

    String getMessage();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
