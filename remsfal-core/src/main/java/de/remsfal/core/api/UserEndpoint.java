package de.remsfal.core.api;

import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.UserJson;
import de.remsfal.core.validation.PatchValidation;

/**
 * @author: Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(UserEndpoint.CONTEXT + "/" + UserEndpoint.VERSION + "/" + UserEndpoint.SERVICE)
public interface UserEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "user";

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of this user identified by the cookie.")
    @APIResponse(responseCode = "200", description = "Information about the logged in user was successfully returned")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    UserJson getUser();

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of this user identified by the cookie.")
    @APIResponse(responseCode = "200", description = "Information about the logged in user was successfully updated")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    UserJson updateUser(
        @Parameter(description = "User information", required = true)
        @Valid @ConvertGroup(to = PatchValidation.class) UserJson user
    );

    @DELETE
    @Operation(summary = "Delete this user identified by the cookie.",
        description = "Organizations in which the user is the last active employee with role OWNER or MANAGER "
            + "are deleted together with the user. Projects in which the user is the only active proprietor, "
            + "directly or via such an organization, have to be deleted or transferred first. The same applies "
            + "to projects managed by such an organization.")
    @APIResponse(responseCode = "204", description = "The former logged in user was deleted successfully")
    @APIResponse(responseCode = "401", description = "No user authentication provided via session cookie")
    @APIResponse(responseCode = "409", description = "The user is the only active proprietor of a project or the "
        + "last active owner or manager of an organization that is required by a project")
    void deleteUser();

}
