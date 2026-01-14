package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.time.Instant;

@Entity("inbox_messages")
public class InboxMessageEntity extends AbstractEntity {

    @Id
    private InboxMessageKey key;

    @Column("issue_id")
    private String issueId;

    @Column("title")
    private String title;

    @Column("issue_type")
    private String issueType;

    @Column("status")
    private String status;

    @Column("description")
    private String description;

    @Column("link")
    private String link;

    @Column("event_type")
    private String eventType;

    @Column("actor_email")
    private String actorEmail;

    @Column("owner_email")
    private String ownerEmail;

    @Column("read")
    private Boolean read;

    public InboxMessageKey getKey() {
        return key;
    }

    public void setKey(InboxMessageKey key) {
        this.key = key;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getActorEmail() {
        return actorEmail;
    }

    public void setActorEmail(String actorEmail) {
        this.actorEmail = actorEmail;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
    
    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }
}
