package de.remsfal.notification.boundary;

import static org.mockito.Mockito.mock;
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

class UserEventConsumerTest {

    private UserEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new UserEventConsumer();
        consumer.logger = mock(Logger.class);
    }

    @Test
    void consume_userDeleted_acknowledgesMessage() {
        final UserEventJson event = ImmutableUserEventJson.builder()
            .userEventType(UserEventType.USER_DELETED)
            .userId(UUID.randomUUID())
            .build();
        final Message<UserEventJson> message = mockMessage(event);

        consumer.consume(message);

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
