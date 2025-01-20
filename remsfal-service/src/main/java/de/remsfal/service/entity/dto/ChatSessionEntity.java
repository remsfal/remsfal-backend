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
    private UUID project_id; // Partition key for horizontal scaling

    @ClusteringColumn
    private UUID sessionId; // Unique ID for the session (Clustering column)

    private UUID taskId; // ID of the associated task
    private String taskType; // Task type (DEFECT, TASK)
    private String status; // Session status (OPEN, CLOSED, ARCHIVED)
    private Map<UUID, String> participants; // Participant ID to role mapping
    private Instant createdAt; // Timestamp of session creation
    private Instant modifiedAt; // Timestamp of last session modification

    // Getters and setters

    @Override
    public UUID getProjectId() {
        return project_id;
    }

    public void setProjectId(UUID project_id) {
        this.project_id = project_id;
    }

    @Override
    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID session_id) {
        this.sessionId = session_id;
    }

    @Override
    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID task_id) {
        this.taskId = task_id;
    }

    @Override
    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String task_type) {
        this.taskType = task_type;
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

    public void setCreatedAt(Instant created_at) {
        this.createdAt = created_at;
    }

    @Override
    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modified_at) {
        this.modifiedAt = modified_at;
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
