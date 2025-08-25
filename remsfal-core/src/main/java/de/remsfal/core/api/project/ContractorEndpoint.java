package de.remsfal.core.api.project;

import de.remsfal.core.json.ContractorJson;
import de.remsfal.core.json.ContractorListJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * API endpoints for contractor management.
 */
public interface ContractorEndpoint {

    String SERVICE = "contractors";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all contractors of a project.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The project does not exist")
    ContractorListJson getContractors(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "Offset of the first contractor to return")
        @QueryParam("offset") @DefaultValue("0") @NotNull @PositiveOrZero Integer offset,
        @Parameter(description = "Maximum number of contractors to return")
        @QueryParam("limit") @DefaultValue("10") @NotNull @Positive @Max(100) Integer limit
    );

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new contractor for a project.")
    @APIResponse(
        responseCode = "201",
        description = "Contractor created successfully",
        headers = @Header(name = "Location", description = "URL of the new contractor")
    )
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The project does not exist")
    Response createContractor(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "Contractor information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) ContractorJson contractor
    );

    @GET
    @Path("/{contractorId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a specific contractor.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The project or contractor does not exist")
    ContractorJson getContractor(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the contractor", required = true)
        @PathParam("contractorId") @NotNull @UUID String contractorId
    );

    @PATCH
    @Path("/{contractorId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a specific contractor.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The project or contractor does not exist")
    ContractorJson updateContractor(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the contractor", required = true)
        @PathParam("contractorId") @NotNull @UUID String contractorId,
        @Parameter(description = "Contractor information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) ContractorJson contractor
    );

    @DELETE
    @Path("/{contractorId}")
    @Operation(summary = "Delete an existing contractor.")
    @APIResponse(responseCode = "204", description = "The contractor was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The project or contractor does not exist")
    void deleteContractor(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "ID of the contractor", required = true)
        @PathParam("contractorId") @NotNull @UUID String contractorId
    );
}
