package de.remsfal.ticketing.boundary.eventing;

import de.remsfal.core.json.ticketing.OcrResultJson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class OcrEventConsumer {

    @Inject
    Logger logger;

    @Incoming("ocr-result")
    public void consume(OcrResultJson message) {
        logger.infov("Received OCR result (sessionId={0}, messageId={1}): {2}",
            message.getSessionId(), message.getMessageId(), message.getExtractedText());
    }
}
