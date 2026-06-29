package de.remsfal.service.control;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import de.remsfal.core.json.eventing.UserEventJson;
import de.remsfal.core.json.eventing.UserEventJson.UserEventType;

class UserEventProducerTest {

    private UserEventProducer producer;
    private Emitter<UserEventJson> emitter;
    private Logger logger;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        producer = new UserEventProducer();
        emitter = mock(Emitter.class);
        logger = mock(Logger.class);
        when(emitter.send(any(UserEventJson.class))).thenReturn(CompletableFuture.completedFuture(null));
        producer.emitter = emitter;
        producer.logger = logger;
    }

    @Test
    void sendUserDeleted_buildsEvent() {
        final UUID userId = UUID.randomUUID();

        producer.sendUserDeleted(userId);

        final ArgumentCaptor<UserEventJson> eventCaptor = ArgumentCaptor.forClass(UserEventJson.class);
        verify(emitter).send(eventCaptor.capture());
        final UserEventJson event = eventCaptor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(UserEventType.USER_DELETED, event.getUserEventType());
        org.junit.jupiter.api.Assertions.assertEquals(userId, event.getUserId());
    }

    @Test
    void sendUserDeleted_withNullUserId_doesNotPublish() {
        producer.sendUserDeleted(null);

        verify(emitter, never()).send(any(UserEventJson.class));
        verify(logger).warn("Skipping user event because userId is null");
    }

    @Test
    void sendUserDeleted_whenEmitterThrows_isHandled() {
        final UUID userId = UUID.randomUUID();
        final RuntimeException failure = new RuntimeException("kafka unavailable");
        Mockito.when(emitter.send(Mockito.<UserEventJson>any())).thenThrow(failure);

        assertDoesNotThrow(() -> producer.sendUserDeleted(userId));
        verify(logger).errorv(
            failure,
            "Error while sending user event (type={0}, userId={1})",
            UserEventType.USER_DELETED,
            userId);
    }
}
