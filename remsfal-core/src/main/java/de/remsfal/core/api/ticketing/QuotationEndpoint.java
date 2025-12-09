package de.remsfal.core.api.ticketing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.QuotationJson;
import de.remsfal.core.validation.PostValidation;

/**
 * Endpoint for managing quotation responses.
 * 
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface QuotationEndpoint {

    String SERVICE = "quotation";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Submit a quotation response for an issue.")
    @APIResponse(responseCode = "201", description = "Quotation created successfully",
        headers = @Header(name = "Location", description = "URL of the new quotation"))
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to submit a quotation")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    Response createQuotation(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "Quotation information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) QuotationJson quotation);

}
