package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.UUID;

public class IssueKey {

    @Id("project_id")
    private UUID projectId;

    @Id("id")
    private UUID id;

    public IssueKey() {
    }

    public IssueKey(UUID projectId, UUID id) {
        this.projectId = projectId;
        this.id = id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IssueKey issueKey = (IssueKey) o;
        return Objects.equals(projectId, issueKey.projectId) && Objects.equals(id, issueKey.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, id);
    }

    @Override
    public String toString() {
        return "IssueKey{" +
                "projectId=" + projectId +
                ", id=" + id +
                '}';
    }
}