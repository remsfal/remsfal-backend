package de.remsfal.chat.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.remsfal.chat.entity.dto.OcrResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OcrEventConsumer {

    @Inject
    ChatMessageController chatMessageController;

    @Inject
    Logger logger;

    private final ObjectMapper mapper = new ObjectMapper();

    @Incoming("ocr-result")
    public void consume(String message) {
        try {
            OcrResult result = mapper.readValue(message, OcrResult.class);
            chatMessageController.updateTextChatMessage(result.sessionId, result.messageId, result.extractedText);
        } catch (Exception e) {
            logger.errorf("Error while parsing the OCR-JSON: %s", e.getMessage());
        }
    }
}
