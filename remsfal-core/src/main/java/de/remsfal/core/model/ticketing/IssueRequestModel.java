package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IssueRequestModel {

    UUID getIssueId();

    UUID getOrganizationId();

    String getMessage();

    List<UUID> getAttachmentIds();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
