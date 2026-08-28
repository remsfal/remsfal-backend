package de.remsfal.core.api.ticketing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.ContractorTimelineJson;
import de.remsfal.core.json.ticketing.ContractorTimelineListJson;

/**
 * Timeline operations for a quotation request, shared by the manager-facing and contractor-facing
 * sub-resources. Both mount this interface as a sub-resource of their own quotation-request
 * endpoint; the implementing boundary class is responsible for enforcing role-exclusive access
 * (see {@code manager.ManagerContractorTimelineResource} for managers and
 * {@code contractor.ContractorTimelineResource} for contractors).
 */
public interface ContractorTimelineEndpoint {

    String SERVICE = "timeline";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the timeline entries for a quotation request.")
    @APIResponse(responseCode = "200", description = "Timeline entries retrieved successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this request")
    @APIResponse(responseCode = "404", description = "The quotation request does not exist")
    ContractorTimelineListJson getTimelineEntries(
        @Parameter(description = "ID of the quotation request", required = true)
        @PathParam("requestId") @NotNull UUID requestId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new timeline entry for a quotation request.")
    @APIResponse(responseCode = "201", description = "Timeline entry created successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to access this request")
    @APIResponse(responseCode = "404", description = "The quotation request does not exist")
    Response createTimelineEntry(
        @Parameter(description = "ID of the quotation request", required = true)
        @PathParam("requestId") @NotNull UUID requestId,
        @Parameter(description = "Timeline entry information", required = true)
        @Valid @NotNull ContractorTimelineJson entry);

}
