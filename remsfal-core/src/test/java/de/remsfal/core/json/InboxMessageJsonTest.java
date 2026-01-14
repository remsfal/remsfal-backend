package de.remsfal.core.json;

import de.remsfal.core.json.ticketing.InboxMessageJson;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InboxMessageJsonTest {

    @Test
    void shouldCreateAndAccessInboxMessageJson() {
        InboxMessageJson msg = new InboxMessageJson();

        msg.id = "123";
        msg.userId = "user-789";

        msg.eventType = "ISSUE_CREATED";
        msg.issueId = "abc-issue-123";
        msg.title = "Critical Bug";
        msg.description = "System crashed";
        msg.issueType = "DEFECT";
        msg.status = "OPEN";
        msg.link = "https://remsfal.de/issues/123";

        msg.actorEmail = "actor@example.com";
        msg.ownerEmail = "owner@example.com";

        msg.read = true;
        msg.createdAt = OffsetDateTime.now();

        assertEquals("123", msg.id);
        assertEquals("user-789", msg.userId);

        assertEquals("ISSUE_CREATED", msg.eventType);
        assertEquals("abc-issue-123", msg.issueId);
        assertEquals("Critical Bug", msg.title);
        assertEquals("System crashed", msg.description);
        assertEquals("DEFECT", msg.issueType);
        assertEquals("OPEN", msg.status);
        assertEquals("https://remsfal.de/issues/123", msg.link);

        assertEquals("actor@example.com", msg.actorEmail);
        assertEquals("owner@example.com", msg.ownerEmail);

        assertTrue(msg.read);
        assertNotNull(msg.createdAt);
    }
}
