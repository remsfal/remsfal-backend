package de.remsfal.ticketing.entity.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssuePriority;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.model.ticketing.IssueModel.IssueType;

class ActivityFeedEntityTest {

    @Test
    void testGetIssuePriority_returnsNullWhenNotSet() {
        final ActivityFeedEntity entity = new ActivityFeedEntity();

        assertNull(entity.getIssuePriority());
    }

    @Test
    void testSetAndGetIssuePriority_roundTripsEnum() {
        final ActivityFeedEntity entity = new ActivityFeedEntity();

        entity.setIssuePriority(IssuePriority.HIGH);

        assertEquals(IssuePriority.HIGH, entity.getIssuePriority());
    }

    @Test
    void testSetIssuePriority_null_clearsValue() {
        final ActivityFeedEntity entity = new ActivityFeedEntity();
        entity.setIssuePriority(IssuePriority.HIGH);

        entity.setIssuePriority((IssuePriority) null);

        assertNull(entity.getIssuePriority());
    }

    @Test
    void testGetActivityType_returnsNullWhenNotSet() {
        final ActivityFeedEntity entity = new ActivityFeedEntity();

        assertNull(entity.getActivityType());
    }

    @Test
    void testSetAndGetActivityType_roundTripsEnum() {
        final ActivityFeedEntity entity = new ActivityFeedEntity();

        entity.setActivityType(IssueEventType.ISSUE_CREATED);

        assertEquals(IssueEventType.ISSUE_CREATED, entity.getActivityType());
    }

    @Test
    void testGetIssueType_returnsNullWhenNotSet() {
        final ActivityFeedEntity entity = new ActivityFeedEntity();

        assertNull(entity.getIssueType());
    }

    @Test
    void testSetAndGetIssueType_roundTripsEnum() {
        final ActivityFeedEntity entity = new ActivityFeedEntity();

        entity.setIssueType(IssueType.DEFECT);

        assertEquals(IssueType.DEFECT, entity.getIssueType());
    }

    @Test
    void testGetIssueStatus_returnsNullWhenNotSet() {
        final ActivityFeedEntity entity = new ActivityFeedEntity();

        assertNull(entity.getIssueStatus());
    }

    @Test
    void testSetAndGetIssueStatus_roundTripsEnum() {
        final ActivityFeedEntity entity = new ActivityFeedEntity();

        entity.setIssueStatus(IssueStatus.OPEN);

        assertEquals(IssueStatus.OPEN, entity.getIssueStatus());
    }

    @Test
    void testGetIdAndUserId_returnNullWhenKeyNotSet() {
        final ActivityFeedEntity entity = new ActivityFeedEntity();

        assertNull(entity.getId());
        assertNull(entity.getUserId());
    }

}
