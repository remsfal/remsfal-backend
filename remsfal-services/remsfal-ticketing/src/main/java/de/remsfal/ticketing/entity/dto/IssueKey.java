package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Embeddable;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public class IssueKey {

    @Id("project_id")
    private UUID projectId;

    @Id("issue_id")
    private UUID issueId;

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(UUID issueId) {
        this.issueId = issueId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssueKey issueKey = (IssueKey) o;
        return Objects.equals(projectId, issueKey.projectId) && Objects.equals(issueId, issueKey.issueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, issueId);
    }

}