package de.remsfal.ticketing.control.events;

import java.time.Instant;
import java.util.UUID;

public class IssuePriorityAlertEvent {

    private int eventVersion = 1;
    private UUID eventId = UUID.randomUUID();
    private Instant emittedAt = Instant.now();

    private UUID issueId;
    private UUID projectId;

    private String priority;
    private Double priorityScore;
    private String priorityModel;

    private String title;

    public int getEventVersion() { return eventVersion; }
    public void setEventVersion(int eventVersion) { this.eventVersion = eventVersion; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public Instant getEmittedAt() { return emittedAt; }
    public void setEmittedAt(Instant emittedAt) { this.emittedAt = emittedAt; }

    public UUID getIssueId() { return issueId; }
    public void setIssueId(UUID issueId) { this.issueId = issueId; }

    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Double getPriorityScore() { return priorityScore; }
    public void setPriorityScore(Double priorityScore) { this.priorityScore = priorityScore; }

    public String getPriorityModel() { return priorityModel; }
    public void setPriorityModel(String priorityModel) { this.priorityModel = priorityModel; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
