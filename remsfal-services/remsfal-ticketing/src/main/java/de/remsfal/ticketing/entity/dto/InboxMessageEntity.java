package de.remsfal.ticketing.entity.dto;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

import java.time.Instant;

@Entity("inbox_messages")
public class InboxMessageEntity extends AbstractEntity {

    @Id
    private InboxMessageKey key;

    @Column("type")
    private String type;

    @Column("received_at")
    private Instant receivedAt;

    @Column("contractor")
    private String contractor;

    @Column("subject")
    private String subject;

    @Column("property")
    private String property;

    @Column("tenant")
    private String tenant;

    @Column("read")
    private Boolean read;

    @Column("issue_link")
    private String issueLink;

    public InboxMessageKey getKey() {
        return key;
    }

    public void setKey(InboxMessageKey key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContractor() {
        return contractor;
    }

    public void setContractor(String contractor) {
        this.contractor = contractor;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public String getIssueLink() {
        return issueLink;
    }

    public void setIssueLink(String issueLink) {
        this.issueLink = issueLink;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}