package de.remsfal.notification.boundary;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.remsfal.core.json.ImmutableUserJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class IssueEventConsumerTest {

    @Inject
    IssueEventConsumer consumer;

    @Test
    void consume_logsReceivedEvent() {
        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .projectId(UUID.randomUUID())
            .title("Test issue")
            .build();

        assertDoesNotThrow(() -> consumer.consume(event));
    }

    @Test
    void consume_handlesMentionedUserEvent() {
        IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.ISSUE_MENTIONED)
            .issueId(UUID.randomUUID())
            .projectId(UUID.randomUUID())
            .title("Another issue")
            .mentionedUser(ImmutableUserJson.builder().id(UUID.randomUUID()).build())
            .build();

        assertDoesNotThrow(() -> consumer.consume(event));
    }
}
