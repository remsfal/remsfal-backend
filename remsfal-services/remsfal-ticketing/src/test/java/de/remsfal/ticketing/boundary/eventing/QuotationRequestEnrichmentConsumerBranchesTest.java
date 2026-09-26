package de.remsfal.ticketing.boundary.eventing;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

import com.datastax.oss.quarkus.test.CassandraTestResource;

import de.remsfal.core.json.eventing.ImmutableIssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.ticketing.control.OrderManagementController;
import io.quarkus.test.InjectMock;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
@QuarkusTestResource(CassandraTestResource.class)
class QuotationRequestEnrichmentConsumerBranchesTest {

    @Inject
    QuotationRequestEnrichmentConsumer consumer;

    @InjectMock
    OrderManagementController controller;

    @Test
    void testConsume_nullPayload_acksWithoutEnrichment() {
        consumer.consume(Message.of((IssueEventJson) null)).toCompletableFuture().join();

        verify(controller, never()).enrichRequestForQuotation(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void testConsume_createdEvent_delegatesToController() {
        final IssueEventJson event = ImmutableIssueEventJson.builder()
            .issueEventType(IssueEventType.QUOTATION_REQUEST_CREATED)
            .issueId(UUID.randomUUID())
            .build();

        consumer.consume(Message.of(event)).toCompletableFuture().join();

        verify(controller).enrichRequestForQuotation(event);
    }
}
