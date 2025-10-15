package de.remsfal.ticketing.control;

import de.remsfal.core.json.ticketing.OcrResultJson;
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

    @Incoming("ocr-result")
    public void consume(OcrResultJson message) {
        logger.infov("Received OCR result: {0}", message);
        chatMessageController.updateTextChatMessage(message.getSessionId(),
            message.getMessageId(), message.getExtractedText());
    }
}
