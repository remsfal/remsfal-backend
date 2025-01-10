package de.remsfal.service.entity.dto;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import de.remsfal.core.model.project.CassChatSessionModel;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for a chat session stored in Cassandra.
 */
@Entity
public class CassChatSessionEntity implements CassChatSessionModel {

    @PartitionKey
    private UUID projectId; // Partition key for horizontal scaling

    @ClusteringColumn
    private UUID sessionId; // Unique ID for the session (Clustering column)

    private UUID taskId; // ID of the associated task

    private String taskType; // Task type (DEFECT, TASK)

    private String status; // Session status (OPEN, CLOSED, ARCHIVED)

    private Map<String, String> participants; // Participant ID to role mapping

    private Date createdAt; // Timestamp of session creation

    private Date modifiedAt; // Timestamp of last session modification

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
    public Map<String, String> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, String> participants) {
        this.participants = participants;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Date getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Date modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
