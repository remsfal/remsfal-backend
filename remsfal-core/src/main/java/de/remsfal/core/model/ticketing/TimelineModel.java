package de.remsfal.core.model.ticketing;

import de.remsfal.core.model.UserContext;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TimelineModel {

    UUID getIssueId();

    UUID getTenancyId();

    UUID getTimelineId();

    UUID getProjectId();

    List<UUID> getAttachmentIds();

    UUID getSenderId();

    String getSenderName();

    UserContext getSenderRole();

    MessagePurpose getPurpose();

    String getMessage();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
