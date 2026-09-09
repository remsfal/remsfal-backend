package de.remsfal.core.api.ticketing.manager;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.ticketing.ActivityFeedJson;
import de.remsfal.core.json.ticketing.ActivityFeedListJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(ActivityFeedEndpoint.CONTEXT + "/" + ActivityFeedEndpoint.VERSION + "/" + ActivityFeedEndpoint.SERVICE)
public interface ActivityFeedEndpoint {

    String CONTEXT = "ticketing";
    String VERSION = "v1";
    String SERVICE = "activities";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the caller's activity feed.",
        description = "Aggregates issue lifecycle events, tenant communication, chat messages, and quotation/"
            + "order placement activity for issues the caller is assigned to, newest first.")
    @APIResponse(responseCode = "200", description = "Activities retrieved successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    ActivityFeedListJson getActivities(
        @Parameter(description = "Filter to return only activities of a specific project")
        @QueryParam("projectId") UUID projectId,
        @Parameter(description = "Filter to return only activities of a specific issue")
        @QueryParam("issueId") UUID issueId,
        @Parameter(description = "Filter to return only activities of a specific rental agreement")
        @QueryParam("agreementId") UUID agreementId,
        @Parameter(description = "Filter to return only activities involving a specific contractor organization")
        @QueryParam("organizationId") UUID organizationId,
        @Parameter(description = "Filter to return only activities involving a specific contractor")
        @QueryParam("contractorId") UUID contractorId,
        @Parameter(description = "Filter to return only activities of issues assigned to a specific user")
        @QueryParam("assigneeId") UUID assigneeId,
        @Parameter(description = "Opaque cursor returned by a previous call to fetch the next page")
        @QueryParam("cursor") UUID cursor,
        @Parameter(description = "Maximum number of activities to return")
        @QueryParam("limit") @DefaultValue("50") @NotNull @Positive @Max(500) Integer limit);

    @PATCH
    @Path("/{activityId}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update the read/unread status of an activity")
    @APIResponse(responseCode = "200", description = "Activity status updated")
    @APIResponse(responseCode = "404", description = "Activity not found for this user")
    ActivityFeedJson updateActivityStatus(
        @Parameter(description = "Activity ID", required = true)
        @PathParam("activityId") @NotNull UUID activityId,
        @Parameter(description = "New read flag: true = read, false = unread", required = true)
        @QueryParam("read") @NotNull Boolean read);

    @DELETE
    @Path("/{activityId}")
    @Operation(summary = "Delete an activity for the authenticated user")
    @APIResponse(responseCode = "204", description = "Activity deleted")
    @APIResponse(responseCode = "404", description = "Activity not found for this user")
    void deleteActivity(
        @Parameter(description = "Activity ID", required = true)
        @PathParam("activityId") @NotNull UUID activityId);

}
