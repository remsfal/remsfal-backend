package de.remsfal.ticketing.boundary.eventing;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.ticketing.control.OrderManagementController;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

/**
 * Consumes enriched {@link IssueEventType#QUOTATION_REQUEST_CREATED} events and completes the
 * quotation request with the project, rental unit and tenant data resolved by the platform service.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@ApplicationScoped
public class QuotationRequestEnrichmentConsumer {

    public static final String CHANNEL = "quotation-request-enrichment";

    @Inject
    OrderManagementController controller;

    @Inject
    Logger logger;

    @Incoming(CHANNEL)
    @Blocking
    @ActivateRequestContext
    public CompletionStage<Void> consume(final Message<IssueEventJson> msg) {
        final IssueEventJson event = msg.getPayload();

        if (event == null) {
            logger.warn("Skipping quotation request enrichment because payload is null (Kafka tombstone)");
            return msg.ack();
        }
        if (event.getIssueEventType() != IssueEventType.QUOTATION_REQUEST_CREATED) {
            return msg.ack();
        }

        controller.enrichRequestForQuotation(event);
        return msg.ack();
    }

}
