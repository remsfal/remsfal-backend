package de.remsfal.core.model.ticketing;

import java.time.Instant;
import java.util.UUID;

public interface TenantTimelineModel {

    UUID getIssueId();

    UUID getTenantId();

    UUID getTimelineId();

    String getUrl();
    
    String getTitle();
    
    String getMessage();
    
    String getRole();
    
    Instant getCreatedAt();

    Instant getModifiedAt();

}