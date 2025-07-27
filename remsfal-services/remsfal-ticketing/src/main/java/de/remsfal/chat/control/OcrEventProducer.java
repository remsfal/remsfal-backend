package de.remsfal.chat.control;

import de.remsfal.chat.entity.dto.OcrRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;


@ApplicationScoped
public class OcrEventProducer {

    @Inject
    Logger logger;

    @Inject
    @Channel("ocr-request")
    Emitter<String> emitter;

    private final ObjectMapper mapper = new ObjectMapper();

    public void sendOcrRequest(String bucket, String fileName, String sessionId, String messageId) {
        try {
            OcrRequest message = new OcrRequest(bucket, fileName, sessionId, messageId);
            String json = mapper.writeValueAsString(message);
            logger.info("Sending OCR request: " + json);
            CompletionStage<Void> ack = emitter.send(json);
            ack.whenComplete((res, ex) -> {
                if (ex != null) {
                    logger.error("Send failed", ex);
                } else {
                    logger.info("Send succeeded");
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OCR request", e);
        }
    }
}
