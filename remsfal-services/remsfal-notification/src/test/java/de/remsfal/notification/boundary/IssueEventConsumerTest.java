package de.remsfal.notification.boundary;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.UUID;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.eventing.ImmutableIssueEventJson;

class IssueEventConsumerTest {

    @Test
    void consume_logsReceivedEvent() {
        IssueEventConsumer consumer = new IssueEventConsumer();
        consumer.logger = Logger.getLogger(IssueEventConsumer.class);

        IssueEventJson event = ImmutableIssueEventJson.builder()
            .type(IssueEventType.ISSUE_CREATED)
            .issueId(UUID.randomUUID())
            .projectId(UUID.randomUUID())
            .title("Test issue")
            .build();

        assertDoesNotThrow(() -> consumer.consume(event));
    }
}
