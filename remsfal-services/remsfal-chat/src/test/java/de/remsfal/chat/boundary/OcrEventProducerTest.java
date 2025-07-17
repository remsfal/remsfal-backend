package de.remsfal.chat.boundary;

import de.remsfal.chat.control.OcrEventProducer;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcrEventProducerTest {

    @Mock
    Emitter<String> emitter;

    @Mock
    Logger logger;

    @InjectMocks
    OcrEventProducer producer;

    @Test
    void testSendOcrRequest_sendFails_logsError() {
        CompletableFuture<Void> failedFuture = new CompletableFuture<>();
        Exception sendException = new RuntimeException("Send failure");
        failedFuture.completeExceptionally(sendException);

        when(emitter.send(anyString())).thenReturn(failedFuture);

        producer.sendOcrRequest("bucket", "file.png", "session123", "msg456");

        Awaitility.await()
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() ->
                        verify(logger).error(eq("Send failed"), eq(sendException))
                );
    }

    @Test
    void testSendOcrRequest_jsonSerializationThrowsException() {
        OcrEventProducer brokenProducer = new OcrEventProducer() {
            @Override
            public void sendOcrRequest(String bucket, String fileName, String sessionId, String messageId) {
                throw new RuntimeException("Serialization failed");
            }
        };

        RuntimeException ex = assertThrows(RuntimeException.class, () -> brokenProducer.sendOcrRequest("bucket", "file", "session", "msg"));

        assertEquals("Serialization failed", ex.getMessage());
    }
}
