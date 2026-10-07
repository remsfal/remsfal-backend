package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.UUID;

public interface IssueRequestModel {

    UUID getIssueRequestId();

    UUID getIssueId();

    UUID getOrganizationId();

    UUID getAgreementId();

    String getMessage();

    Instant getCreatedAt();

    Instant getModifiedAt();

}
