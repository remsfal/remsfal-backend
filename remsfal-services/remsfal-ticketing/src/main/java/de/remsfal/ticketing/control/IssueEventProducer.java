package de.remsfal.ticketing.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.remsfal.ticketing.control.events.IssueCreatedEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Metadata;
import org.jboss.logging.Logger;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;


@ApplicationScoped
public class IssueEventProducer {

    @Inject
    Logger logger;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @Channel("tickets-outgoing")
    Emitter<String> emitter;

    public void sendIssueCreated(final IssueCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            var metadata = Metadata.of(OutgoingKafkaRecordMetadata.<String>builder()
                    .withKey(event.getIssueId().toString())
                    .build());
            emitter.send(Message.of(payload, metadata));
            logger.infov("Sent issue created event for issueId={0}", event.getIssueId());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize issue created event", e);
        }
    }
}