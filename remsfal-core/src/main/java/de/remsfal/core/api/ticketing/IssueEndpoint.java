package de.remsfal.core.api.ticketing;

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

import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.IssueListJson;
import de.remsfal.core.model.ticketing.IssueModel.Status;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;
import de.remsfal.core.validation.UUID;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(IssueEndpoint.CONTEXT + "/" + IssueEndpoint.VERSION + "/" + IssueEndpoint.SERVICE)
public interface IssueEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "issues";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all issues.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    IssueListJson getIssues(
        @Parameter(description = "Offset of the first project to return")
        @QueryParam("offset") @DefaultValue("0") @NotNull @PositiveOrZero Integer offset,
        @Parameter(description = "Maximum number of projects to return")
        @QueryParam("limit") @DefaultValue("50") @NotNull @Positive @Max(500) Integer limit,
        @Parameter(description = "Filter to return only issues of a specific project")
        @QueryParam("projectId") @NotNull @UUID String projectId,
        @Parameter(description = "Filter to return only issues of a specific user")
        @QueryParam("owner") @UUID String ownerId,
        @Parameter(description = "Filter to return only issues with a specific status")
        @QueryParam("status") Status status);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new issue.")
    @APIResponse(responseCode = "201", description = "Issue created successfully",
        headers = @Header(name = "Location", description = "URL of the new issue"))
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createIssue(
        @Parameter(description = "Issue information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) IssueJson issue);

    @GET
    @Path("/{issueId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a issue.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    IssueJson getIssue(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull @UUID String issueId);

    @PATCH
    @Path("/{issueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a issue.")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    IssueJson updateIssue(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull @UUID String issueId,
        @Parameter(description = "Issue information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) IssueJson issue);

    @DELETE
    @Path("/{issueId}")
    @Operation(summary = "Delete an existing issue.")
    @APIResponse(responseCode = "204", description = "The issue was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    void deleteIssue(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull @UUID String issueId);


    @Path("/{issueId}/" + ChatSessionEndpoint.SERVICE)
    ChatSessionEndpoint getChatSessionResource();

}
