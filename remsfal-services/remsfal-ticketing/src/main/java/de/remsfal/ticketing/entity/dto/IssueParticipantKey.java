package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Embeddable;

import java.util.UUID;

@Embeddable
public class IssueParticipantKey {

    @Column("user_id")
    private UUID userId;

    @Column("issue_id")
    private UUID issueId;

    @Column("session_id")
    private UUID sessionId;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(UUID issueId) {
        this.issueId = issueId;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }
}