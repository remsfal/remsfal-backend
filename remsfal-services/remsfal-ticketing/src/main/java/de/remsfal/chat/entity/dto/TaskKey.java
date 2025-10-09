package de.remsfal.chat.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Id;

import java.util.Objects;
import java.util.UUID;

public class TaskKey {

    @Id("project_id")
    private UUID projectId;

    @Id("id")
    private UUID id;

    public TaskKey() {
    }

    public TaskKey(UUID projectId, UUID id) {
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
        TaskKey taskKey = (TaskKey) o;
        return Objects.equals(projectId, taskKey.projectId) && Objects.equals(id, taskKey.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, id);
    }

    @Override
    public String toString() {
        return "TaskKey{" +
                "projectId=" + projectId +
                ", id=" + id +
                '}';
    }
}