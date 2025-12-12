package de.remsfal.ticketing.boundary;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

import org.jboss.logging.Logger;

import de.remsfal.core.api.ticketing.QuotationEndpoint;
import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.model.ticketing.QuotationModel;
import de.remsfal.ticketing.control.QuotationController;
import io.quarkus.security.Authenticated;

/**
 * Resource implementation for quotation endpoints.
 * 
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Authenticated
@RequestScoped
public class QuotationResource extends AbstractResource implements QuotationEndpoint {

    @Inject
    Logger logger;

    @Inject
    QuotationController quotationController;

    @Override
    public Response createQuotation(@PathParam("issueId") final UUID issueId, 
            final QuotationJson quotation) {
        logger.infov("Creating quotation for issue {0}", issueId);
        
        // Create the quotation
        final QuotationModel createdQuotation = quotationController.createQuotation(
            principal, issueId, quotation);
        
        // Build the response
        final QuotationJson response = QuotationJson.valueOf(createdQuotation);
        final URI location = uri.getAbsolutePathBuilder()
            .path(createdQuotation.getId().toString())
            .build();
        
        return Response.created(location)
            .type(MediaType.APPLICATION_JSON)
            .entity(response)
            .build();
    }

}
