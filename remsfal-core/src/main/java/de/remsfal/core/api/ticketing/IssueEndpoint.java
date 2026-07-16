package de.remsfal.core.api.ticketing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.IssueJson;
import de.remsfal.core.json.ticketing.IssueListJson;
import de.remsfal.core.model.RentalUnitModel.UnitType;
import de.remsfal.core.model.ticketing.IssueModel.IssueStatus;
import de.remsfal.core.validation.PatchValidation;
import de.remsfal.core.validation.PostValidation;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(IssueEndpoint.CONTEXT + "/" + IssueEndpoint.VERSION + "/" + IssueEndpoint.SERVICE)
public interface IssueEndpoint {

    String CONTEXT = "ticketing";
    String VERSION = "v1";
    String SERVICE = "issues";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information for all issues of a single project.",
        description = "This method is intended solely for use by a property manager, scoped to exactly one"
        + " project at a time. Tenants must use the separate tenant issues endpoint instead.")
    @APIResponse(responseCode = "200", description = "Issues retrieved successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to read issues of this project")
    IssueListJson getIssues(
        @Parameter(description = "ID of the project to return issues of", required = true)
        @QueryParam("projectId") @NotNull UUID projectId,
        @Parameter(description = "Filter to return only issues of a assigned user")
        @QueryParam("assigneeId") UUID assigneeId,
        @Parameter(description = "Filter to return only issuesfor a specific rental agreement")
        @QueryParam("agreementId") UUID agreementId,
        @Parameter(description = "Filter to return only issuesfor a specific unit type")
        @QueryParam("rentalUnitType") UnitType rentalUnitType,
        @Parameter(description = "Filter to return only issuesfor a specific rental unit")
        @QueryParam("rentalUnitId") UUID rentalUnitId,
        @Parameter(description = "Filter to return only issues with a specific status")
        @QueryParam("status") IssueStatus status,
        @Parameter(description = "Opaque cursor returned by a previous call to fetch the next page")
        @QueryParam("cursor") String cursor,
        @Parameter(description = "Maximum number of issues to return")
        @QueryParam("limit") @DefaultValue("50") @NotNull @Positive @Max(500) Integer limit);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new issue.",
        description = "Creates a new issue based on the provided issue information."
        + " This method is intended solely for the creation of issues by a property manager.")
    @APIResponse(responseCode = "201", description = "Issue created successfully",
        headers = @Header(name = "Location", description = "URL of the new issue"),
        content = @Content(mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = IssueJson.class)))
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    Response createProjectIssue(
        @Parameter(description = "Issue information", required = true)
        @Valid @ConvertGroup(to = PostValidation.class) IssueJson issue);

    @GET
    @Path("/{issueId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of an issue.",
        description = "This method is intended solely for use by a property manager.")
    @APIResponse(responseCode = "200", description = "Issue retrieved successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "User does not have permission to view this issue")
    @APIResponse(responseCode = "404", description = "The property does not exist")
    IssueJson getIssue(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @PATCH
    @Path("/{issueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of an issue.")
    @APIResponse(responseCode = "200", description = "Issue updated successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "404", description = "The issue does not exist")
    IssueJson updateIssue(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "Issue information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) IssueJson issue);

    @DELETE
    @Path("/{issueId}")
    @Operation(summary = "Delete an existing issue.")
    @APIResponse(responseCode = "204", description = "The issue was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    void deleteIssue(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId);

    @PUT
    @Path("/{issueId}/parent/{parentIssueId}")
    @Operation(summary = "Set another issue as parent issue.")
    @APIResponse(responseCode = "200", description = "Successfully updated issue")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    IssueJson setParent(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "ID of the parent issue", required = true)
        @PathParam("parentIssueId") @NotNull UUID parentIssueId);

    @POST
    @Path("/{issueId}/{relationType: children|blocks|blocked-by|related-to|duplicate-of}/{relatedIssueId}")
    @Operation(summary = "Create a relation between two issues.")
    @APIResponse(responseCode = "200", description = "Successfully updated issue")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    IssueJson createRelation(
        @Parameter(description = "ID of the issue", required = true)
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "Type of the relation", examples = {
            @ExampleObject("children"),
            @ExampleObject("blocks"),
            @ExampleObject("blocked-by"),
            @ExampleObject("related-to"),
            @ExampleObject("duplicate-of")
        })
        @PathParam("relationType") @NotNull String relationType,
        @Parameter(description = "ID of the related issue", required = true)
        @PathParam("relatedIssueId") @NotNull UUID relatedIssueId);

    @DELETE
    @Path("/{issueId}/{relationType: parent|children|blocks|blocked-by|related-to|duplicate-of}/{relatedIssueId}")
    @Operation(summary = "Delete an existing relation between two issues")
    @APIResponse(responseCode = "204", description = "The relation was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    void deleteRelation(
        @Parameter(description = "ID of the source Issue")
        @PathParam("issueId") @NotNull UUID issueId,
        @Parameter(description = "Type of the relation", examples = {
            @ExampleObject("parent"),
            @ExampleObject("children"),
            @ExampleObject("blocks"),
            @ExampleObject("blocked-by"),
            @ExampleObject("related-to"),
            @ExampleObject("duplicate-of")
        })
        @PathParam("relationType") @NotNull String relationType,
        @Parameter(description = "ID of the related Issue")
        @PathParam("relatedIssueId") @NotNull UUID relatedIssueId
    );

    @Path("/{issueId}/" + IssueAttachmentEndpoint.SERVICE)
    IssueAttachmentEndpoint getAttachmentResource();

    @Path("/{issueId}/" + IssueQuotationRequestEndpoint.SERVICE)
    IssueQuotationRequestEndpoint getQuotationRequestResource();

    @Path("/{issueId}/" + IssueQuotationEndpoint.SERVICE)
    IssueQuotationEndpoint getQuotationResource();

    @Path("/{issueId}/" + ChatSessionEndpoint.SERVICE)
    ChatSessionEndpoint getChatSessionResource();

    @Path("/{issueId}/" + TimelineEndpoint.SERVICE)
    TimelineEndpoint getTimelineResource();

}
