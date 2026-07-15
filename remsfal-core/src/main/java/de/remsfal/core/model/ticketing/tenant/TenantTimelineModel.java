package de.remsfal.core.model.ticketing.tenant;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TenantTimelineModel {

    UUID getIssueId();

    UUID getTenancyId();

    UUID getTimelineId();
    
    UUID getProjectId();
    
    List<UUID> getAttachmentIds();
    
    UUID getSenderId();
    
    String getSenderName();
    
    MessagePurpose getPurpose();

    String getMessage();
    
    Instant getCreatedAt();

    Instant getModifiedAt();

}
