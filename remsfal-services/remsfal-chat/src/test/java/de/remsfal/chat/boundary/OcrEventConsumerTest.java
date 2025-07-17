package de.remsfal.chat.boundary;

import de.remsfal.chat.control.OcrEventConsumer;
import de.remsfal.chat.control.ChatMessageController;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@QuarkusTest
public class OcrEventConsumerTest {

    @Inject
    OcrEventConsumer consumer;

    @InjectMock
    ChatMessageController chatMessageController;

    @Test
    public void testConsumerWithInvalidJson() {
        String invalidJson = "{invalid json}";

        consumer.consume(invalidJson);

        verify(chatMessageController, never())
                .updateTextChatMessage(any(), any(), any());
    }
}