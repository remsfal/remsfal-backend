package de.remsfal.ticketing.control;

import de.remsfal.core.json.ticketing.FileUploadJson;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class OcrEventProducer {

    @Inject
    Logger logger;

    @Inject
    @Channel("ocr-request")
    Emitter<FileUploadJson> emitter;

    public void sendOcrRequest(final FileUploadJson uploadedFile) {
        try {
            logger.infov("Sending OCR request: {0}", uploadedFile);
            CompletionStage<Void> ack = emitter.send(uploadedFile);
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
