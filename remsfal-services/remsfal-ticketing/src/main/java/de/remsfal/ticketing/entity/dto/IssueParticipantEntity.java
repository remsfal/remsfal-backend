package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("issue_participants")
public class IssueParticipantEntity extends AbstractEntity {

    public enum ParticipantRole {
        INITIATOR,   // User who created the issue/started the chat
        HANDLER,     // User assigned to handle/resolve the issue
        OBSERVER;    // User who observes but doesn't actively handle

        public static ParticipantRole fromString(String role) {
            if (role == null) {
                return null;
            }
            try {
                return ParticipantRole.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid participant role: " + role);
            }
        }
    }

    @Id
    private IssueParticipantKey key;

    @Column("project_id")
    private UUID projectId;

    @Column("role")
    private String role;  // Stored as String in DB for compatibility

    public IssueParticipantKey getKey() {
        return key;
    }

    public void setKey(IssueParticipantKey key) {
        this.key = key;
    }

    public UUID getUserId() {
        return Optional.ofNullable(key).map(IssueParticipantKey::getUserId).orElse(null);
    }

    public UUID getIssueId() {
        return Optional.ofNullable(key).map(IssueParticipantKey::getIssueId).orElse(null);
    }

    public UUID getSessionId() {
        return Optional.ofNullable(key).map(IssueParticipantKey::getSessionId).orElse(null);
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    // Type-safe getter and setter
    public ParticipantRole getParticipantRole() {
        return ParticipantRole.fromString(role);
    }

    public void setParticipantRole(ParticipantRole participantRole) {
        this.role = participantRole != null ? participantRole.name() : null;
    }
}