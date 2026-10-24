package de.remsfal.core.api.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.notification.EmailSettingsJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(EmailSettingsEndpoint.CONTEXT + "/" + EmailSettingsEndpoint.VERSION + "/" + EmailSettingsEndpoint.SERVICE)
public interface EmailSettingsEndpoint {

    String CONTEXT = "notification";
    String VERSION = "v1";
    String SERVICE = "settings/issues/emails";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve the issue email preferences of the authenticated manager.",
        description = "Returns which issue emails the caller receives. Without stored preferences, all emails "
            + "are enabled. Mandatory emails (e.g. project admission) are not configurable and not listed.")
    @APIResponse(responseCode = "200", description = "Email preferences retrieved successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "The caller is not a manager of any project")
    EmailSettingsJson getEmailSettings();

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update the issue email preferences of the authenticated manager.",
        description = "Only the provided flags are changed, omitted flags keep their current value.")
    @APIResponse(responseCode = "200", description = "Email preferences updated successfully")
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "403", description = "The caller is not a manager of any project")
    EmailSettingsJson updateEmailSettings(
        @RequestBody(description = "Email preferences to change", required = true)
        @Valid @NotNull EmailSettingsJson settings);

}
