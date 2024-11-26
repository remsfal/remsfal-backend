package de.remsfal.service.entity.dto;

import jakarta.persistence.*;
import de.remsfal.core.model.project.ChatSessionModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.*;

@Entity
@Table(name = "CHAT_SESSION")
public class ChatSessionEntity extends AbstractEntity implements ChatSessionModel {

    @Id
    @Column(name = "ID", columnDefinition = "char", nullable = false, length = 36)
    @NotBlank
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "PROJECT_ID", columnDefinition = "char", nullable = false, length = 36)
    @NotBlank
    private String projectId;

    @Override
    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    @Column(name = "TASK_ID", columnDefinition = "char", nullable = false, length = 36)
    @NotBlank
    private String taskId;

    @Override
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "TASK_TYPE", nullable = false)
    @NotNull
    private TaskType taskType;

    @Override
    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    /**
     * Participants map: participantId -> ParticipantRole
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "CHAT_SESSION_PARTICIPANT", joinColumns = @JoinColumn(name = "CHAT_SESSION_ID"))
    @MapKeyColumn(name = "PARTICIPANT_ID", columnDefinition = "char(36)", length = 36)
    @Column(name = "ROLE")
    @Enumerated(EnumType.STRING)
    private Map<String, ParticipantRole> participants = new HashMap<>();

    @Override
    public Map<String, ParticipantRole> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, ParticipantRole> participants) {
        this.participants = participants;
    }

    @OneToMany(mappedBy = "chatSession", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("createdAt ASC") // Ensure consistent order
    private List<ChatMessageEntity> messages = new ArrayList<>();


    @Override
    public List<ChatMessageEntity> getMessages() {
        return messages;
    }

    // Status of the chat session: OPEN, CLOSED, ARCHIVED
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    @NotNull
    private Status status;

    @Override
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }




    @Override
    public Date getCreatedAt() {
        return super.getCreatedAt();
    }

    @Override
    public Date getModifiedAt() {
        return super.getModifiedAt();
    }

    public void addMessage(ChatMessageEntity message) {
        if (messages == null) {
            messages = new ArrayList<>();
        }
        messages.add(message);
        message.setChatSession(this); // Ensures bidirectionality
    }


    // Equals and hashCode methods
    @Override
    public int hashCode() {
        return Objects.hash(id, projectId, taskId, taskType, status, getCreatedAt(), getModifiedAt());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChatSessionEntity that)) return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(projectId, that.projectId) &&
                Objects.equals(taskId, that.taskId) &&
                taskType == that.taskType &&
                status == that.status &&
                Objects.equals(getCreatedAt(), that.getCreatedAt()) &&
                Objects.equals(getModifiedAt(), that.getModifiedAt());
    }

}
