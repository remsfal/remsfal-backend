package de.remsfal.ticketing.control;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.remsfal.core.json.eventing.ImmutableUserEventJson;
import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.ticketing.entity.dao.IssueRepository;

class UserEventConsumerTest {

    private UserEventConsumer consumer;
    private IssueRepository issueRepository;
    private Logger logger;

    @BeforeEach
    void setUp() {
        consumer = new UserEventConsumer();
        issueRepository = mock(IssueRepository.class);
        logger = mock(Logger.class);
        consumer.issueRepository = issueRepository;
        consumer.logger = logger;
    }

    @Test
    void consume_userDeleted_updatesIssues() {
        final UUID userId = UUID.randomUUID();
        final UserEventJson event = ImmutableUserEventJson.builder()
            .userEventType(UserEventType.USER_DELETED)
            .userId(userId)
            .build();
        final Message<UserEventJson> message = mockMessage(event);
        when(issueRepository.clearAssigneeAndResetStatus(userId, IssueStatus.OPEN)).thenReturn(2);

        consumer.consume(message);

        verify(issueRepository).clearAssigneeAndResetStatus(userId, IssueStatus.OPEN);
        verify(message).ack();
    }

    @Test
    void consume_incompletePayload_isSkipped() {
        final Message<UserEventJson> message = mockMessage(null);

        consumer.consume(message);

        verify(issueRepository, never()).clearAssigneeAndResetStatus(any(), any());
        verify(logger).warn("Skipping user event because payload is incomplete");
        verify(message).ack();
    }

    @Test
    void consume_payloadWithoutUserId_isSkipped() {
        final UserEventJson event = mock(UserEventJson.class);
        when(event.getUserEventType()).thenReturn(UserEventType.USER_DELETED);
        when(event.getUserId()).thenReturn(null);
        final Message<UserEventJson> message = mockMessage(event);

        consumer.consume(message);

        verify(issueRepository, never()).clearAssigneeAndResetStatus(any(), any());
        verify(logger).warn("Skipping user event because payload is incomplete");
        verify(message).ack();
    }

    @SuppressWarnings("unchecked")
    private Message<UserEventJson> mockMessage(final UserEventJson payload) {
        final Message<UserEventJson> message = mock(Message.class);
        when(message.getPayload()).thenReturn(payload);
        when(message.ack()).thenReturn(CompletableFuture.completedFuture(null));
        return message;
    }
}
