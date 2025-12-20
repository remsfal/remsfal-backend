package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.util.Optional;
import java.util.UUID;

@Entity("issue_participants")
public class IssueParticipantEntity extends AbstractEntity {

    @Id
    private IssueParticipantKey key;

    @Column("project_id")
    private UUID projectId;

    @Column("role")
    private String role;


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


}