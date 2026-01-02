package de.remsfal.ticketing.control.events;

import java.time.Instant;
import java.util.UUID;

public class IssuePriorityResultEvent {

    private UUID issueId;
    private UUID projectId;
    private String priority;
    private Double priorityScore;
    private String priorityModel;
    private Instant priorityTimestamp;

    public UUID getIssueId() {
        return issueId;
    }

    public void setIssueId(UUID issueId) {
        this.issueId = issueId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Double getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(Double priorityScore) {
        this.priorityScore = priorityScore;
    }

    public String getPriorityModel() {
        return priorityModel;
    }

    public void setPriorityModel(String priorityModel) {
        this.priorityModel = priorityModel;
    }

    public Instant getPriorityTimestamp() {
        return priorityTimestamp;
    }

    public void setPriorityTimestamp(Instant priorityTimestamp) {
        this.priorityTimestamp = priorityTimestamp;
    }
}