package de.remsfal.notification.entity.dto;

import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import de.remsfal.common.entity.MetaDataEntity;
import de.remsfal.core.model.notification.EmailSettingsModel;

/**
 * Email preferences of a user, one row per user. A missing row means that all emails are enabled.
 */
@Entity
@Table(name = "user_email_settings")
public class UserEmailSettingsEntity extends MetaDataEntity implements EmailSettingsModel {

    @Id
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "issue_created", nullable = false)
    private boolean issueCreated = true;

    @Column(name = "issue_updated", nullable = false)
    private boolean issueUpdated = true;

    @Column(name = "issue_assigned", nullable = false)
    private boolean issueAssigned = true;

    @Column(name = "issue_message_created", nullable = false)
    private boolean issueMessageCreated = true;

    @Override
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(final UUID userId) {
        this.userId = userId;
    }

    @Override
    public Boolean isIssueCreated() {
        return issueCreated;
    }

    public void setIssueCreated(final boolean issueCreated) {
        this.issueCreated = issueCreated;
    }

    @Override
    public Boolean isIssueUpdated() {
        return issueUpdated;
    }

    public void setIssueUpdated(final boolean issueUpdated) {
        this.issueUpdated = issueUpdated;
    }

    @Override
    public Boolean isIssueAssigned() {
        return issueAssigned;
    }

    public void setIssueAssigned(final boolean issueAssigned) {
        this.issueAssigned = issueAssigned;
    }

    @Override
    public Boolean isIssueMessageCreated() {
        return issueMessageCreated;
    }

    public void setIssueMessageCreated(final boolean issueMessageCreated) {
        this.issueMessageCreated = issueMessageCreated;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof UserEmailSettingsEntity e) {
            return super.equals(e)
                && Objects.equals(userId, e.userId)
                && issueCreated == e.issueCreated
                && issueUpdated == e.issueUpdated
                && issueAssigned == e.issueAssigned
                && issueMessageCreated == e.issueMessageCreated;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

}
