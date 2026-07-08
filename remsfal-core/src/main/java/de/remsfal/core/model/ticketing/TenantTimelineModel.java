package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TenantTimelineModel {

    UUID getIssueId();

    UUID getTenancyId();

    UUID getTimelineId();
    
    UUID getProjectId();
    
    List<UUID> getAttachmentId();
    
    UUID getSenderId();
    
    String getSenderName();
    
    String getTitle();
    
    String getMessage();
    
    Instant getCreatedAt();

    Instant getModifiedAt();

}
