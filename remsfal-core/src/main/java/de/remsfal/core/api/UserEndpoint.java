package de.remsfal.core.api;

import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.json.UserJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(UserEndpoint.CONTEXT + "/" + UserEndpoint.VERSION + "/" + UserEndpoint.SERVICE)
public interface UserEndpoint {

    final static String CONTEXT = "api";
    final static String VERSION = "v1";
    final static String SERVICE = "user";

    @POST
    @Operation(summary = "Registers a new user identified by the token.")
    @APIResponse(responseCode = "200", description = "User created successfully")
    @APIResponse(responseCode = "400", description = "Invalid request message")
    @APIResponse(responseCode = "409", description = "Another user with the same email already exist")
    UserJson registerUser();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of this user identified by the token.")
    @APIResponse(responseCode = "404", description = "The user does not exist")
    UserJson getUser();

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of this user identified by the token.")
    @APIResponse(responseCode = "404", description = "The user does not exist")
    UserJson updateUser(@Parameter(description = "User information", required = true) @Valid UserJson user);

    @DELETE
    @Operation(summary = "Delete this user identified by the token.")
    @APIResponse(responseCode = "204", description = "The user was deleted successfully")
    @APIResponse(responseCode = "404", description = "The user does not exist")
    void deleteUser();

}
