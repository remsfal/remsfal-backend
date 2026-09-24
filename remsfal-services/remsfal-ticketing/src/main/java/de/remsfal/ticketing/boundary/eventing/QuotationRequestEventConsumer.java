package de.remsfal.ticketing.boundary.eventing;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.remsfal.core.json.eventing.IssueEventJson;
import de.remsfal.core.json.eventing.IssueEventJson.IssueEventType;
import de.remsfal.core.json.project.RentalAgreementJson;
import de.remsfal.core.json.project.TenantJson;
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
 * Consumes enriched quotation request events and stores the contact details of the tenants of the
 * issue's rental agreement on the quotation request, so the contractor can arrange appointments.
 */
@ApplicationScoped
public class QuotationRequestEventConsumer {

    static final String CHANNEL = "quotation-request-tenants";

    @Inject
    OrderManagementController orderManagementController;

    @Inject
    Logger logger;

    @Incoming(CHANNEL)
    @Blocking
    @ActivateRequestContext
    public CompletionStage<Void> consume(final Message<IssueEventJson> msg) {
        final IssueEventJson event = msg.getPayload();

        if (event == null || event.getIssueEventType() != IssueEventType.QUOTATION_REQUEST_CREATED
            || event.getQuotationRequest() == null || event.getQuotationRequest().getId() == null) {
            return msg.ack();
        }

        final List<String> tenants = tenantContacts(event.getRentalAgreement());
        if (tenants.isEmpty()) {
            logger.infof("No tenant contacts to store for quotation request %s", event.getQuotationRequest().getId());
            return msg.ack();
        }

        orderManagementController.storeTenantContacts(event.getIssueId(), event.getQuotationRequest().getId(),
            tenants);
        return msg.ack();
    }

    private List<String> tenantContacts(final RentalAgreementJson agreement) {
        if (agreement == null || agreement.getTenants() == null) {
            return List.of();
        }
        return agreement.getTenants().stream()
            .map(this::tenantContact)
            .filter(contact -> !contact.isEmpty())
            .toList();
    }

    private String tenantContact(final TenantJson tenant) {
        final String name = Stream.of(tenant.getFirstName(), tenant.getLastName())
            .filter(QuotationRequestEventConsumer::isPresent)
            .collect(Collectors.joining(" "));
        final String phone = Stream.of(tenant.getMobilePhoneNumber(), tenant.getPrivatePhoneNumber(),
                tenant.getBusinessPhoneNumber())
            .filter(QuotationRequestEventConsumer::isPresent)
            .findFirst()
            .map(number -> "Tel. " + number)
            .orElse(null);
        return Stream.of(name, phone, tenant.getEmail())
            .filter(Objects::nonNull)
            .filter(QuotationRequestEventConsumer::isPresent)
            .collect(Collectors.joining(", "));
    }

    private static boolean isPresent(final String value) {
        return value != null && !value.isBlank();
    }

}
