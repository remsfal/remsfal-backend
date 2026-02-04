package de.remsfal.core.api.project;

import de.remsfal.core.json.tenancy.ProjectRentalAgreementListJson;
import de.remsfal.core.json.tenancy.RentalAgreementInfoJson;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * @author Carl Rix [Carl.Rix@student.htw-berlin.de]
 */
public interface RentalAgreementEndpoint {

    String SERVICE = "rental-agreements";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of all tenancies")
    @APIResponse(responseCode = "200", description = "The requested tenancies were successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    ProjectRentalAgreementListJson getRentalAgreements(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new tenancy")
    @APIResponse(responseCode = "201", description = "Tenancy created successfully")
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createRentalAgreement(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "Tenancy Information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) RentalAgreementInfoJson tenancy);

    @GET
    @Path("/{agreementId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve Information of a tenancy.")
    @APIResponse(responseCode = "200", description = "The requested rental agreement was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided session cookie")
    @APIResponse(responseCode = "404", description = "The rental agreement does not exist")
    RentalAgreementInfoJson getRentalAgreement(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("agreementId") @NotNull UUID agreementId);

    @PATCH
    @Path("/{agreementId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a tenancy")
    @APIResponse(responseCode = "200", description = "The rental agreement was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The rental agreement does not exist")
    RentalAgreementInfoJson updateRentalAgreement(
        @Parameter(description = "ID of the project", required = true)
        @PathParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("agreementId") @NotNull UUID agreementId,
        @Parameter(description = "Tenancy information", required = true)
        @Valid @NotNull @ConvertGroup(to = PatchValidation.class) RentalAgreementInfoJson tenancy);
}
