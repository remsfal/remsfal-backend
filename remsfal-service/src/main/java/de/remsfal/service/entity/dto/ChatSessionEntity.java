package de.remsfal.service.entity.dto;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import de.remsfal.core.model.project.ChatSessionModel;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for a chat session stored in Cassandra.
 */
@Entity
public class ChatSessionEntity implements ChatSessionModel {

    @PartitionKey
    private UUID projectId;
    @ClusteringColumn
    private UUID sessionId;
    private UUID taskId;
    private String taskType;
    private String status;
    private Map<UUID, String> participants;
    private Instant createdAt;
    private Instant modifiedAt;

    // Getters and setters

    @Override
    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    @Override
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public Map<UUID, String> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<UUID, String> participants) {
        this.participants = participants;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    /**
     * Maps a Cassandra row to a `CassChatSessionEntity`.
     *
     * @param row The Cassandra row.
     * @return The mapped entity.
     */
    public static ChatSessionEntity mapRow(Row row) {
        ChatSessionEntity entity = new ChatSessionEntity();
        entity.setProjectId(row.getUuid("project_id"));
        entity.setSessionId(row.getUuid("session_id"));
        entity.setTaskId(row.getUuid("task_id"));
        entity.setTaskType(row.getString("task_type"));
        entity.setStatus(row.getString("status"));
        entity.setParticipants(row.getMap("participants", UUID.class, String.class));
        entity.setCreatedAt(row.getInstant("created_at"));
        entity.setModifiedAt(row.getInstant("modified_at"));
        return entity;
    }
}
