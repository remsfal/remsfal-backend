package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.UUID;

public interface ChatMessageModel {

    UUID getProjectId();

    UUID getIssueId();

    UUID getMessageId();

    UUID getSenderId();

    String getSenderName();

    String getMessage();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
