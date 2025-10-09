package de.remsfal.core.api.tenancy;

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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.tenancy.IssueJson;
import de.remsfal.core.json.tenancy.IssueListJson;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public interface IssueEndpoint {

    String SERVICE = "issues";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all issues.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    IssueListJson getIssues(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull UUID tenancyId,
        @Parameter(description = "Type of the rental", required = true)
        @PathParam("rentalType") @NotNull String rentalType,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull UUID rentalId,
        @Parameter(description = "Filter to return only issues with a specific status")
        @QueryParam("status") Status status);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new issue.")
    @APIResponse(responseCode = "201", description = "Issue created successfully",
        headers = @Header(name = "Location", description = "URL of the new issue"))
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createIssue(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull UUID tenancyId,
        @Parameter(description = "Type of the rental", required = true)
        @PathParam("rentalType") @NotNull String rentalType,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull UUID rentalId,
        @Parameter(description = "Issue information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) IssueJson issue);

    @GET
    @Path("/{issueId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of an issue.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The property does not exist")
    IssueJson getIssue(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull UUID tenancyId,
        @Parameter(description = "Type of the rental", required = true)
        @PathParam("rentalType") @NotNull String rentalType,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull UUID rentalId,
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @PATCH
    @Path("/{issueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of an issue.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    IssueJson updateIssue(
        @Parameter(description = "ID of the tenancy", required = true)
        @PathParam("tenancyId") @NotNull UUID tenancyId,
        @Parameter(description = "Type of the rental", required = true)
        @PathParam("rentalType") @NotNull String rentalType,
        @Parameter(description = "ID of the rental", required = true)
        @PathParam("rentalId") @NotNull UUID rentalId,
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "Issue information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) IssueJson issue);

}
