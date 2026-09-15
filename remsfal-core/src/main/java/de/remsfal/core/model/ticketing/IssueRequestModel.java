package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface IssueRequestModel {

    UUID getIssueRequestId();

    UUID getIssueId();

    UUID getOrganizationId();

    UUID getAgreementId();

    String getMessage();

    List<UUID> getAttachmentIds();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
