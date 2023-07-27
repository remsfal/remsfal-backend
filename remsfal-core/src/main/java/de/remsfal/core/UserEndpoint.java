package de.remsfal.core;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import de.remsfal.core.dto.UserJson;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(UserEndpoint.CONTEXT + "/" + UserEndpoint.VERSION + "/" + UserEndpoint.SERVICE)
public interface UserEndpoint {

    final static String CONTEXT = "api";
    final static String VERSION = "v1";
    final static String SERVICE = "users";
    @GET
    @Path("/authenticate")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve information of a user.")
    UserJson authenticate(
            @Parameter(in = ParameterIn.HEADER, description = "Authorization header", required = true) @HeaderParam("Authorization") String authHeader);


    @PATCH
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update information of a user.")
    @APIResponse(responseCode = "404", description = "The user does not exist")
    UserJson updateUser(
            @Parameter(description = "ID of the user", required = true) @PathParam("userId") String userId,
            @Parameter(description = "User information", required = true) @Valid UserJson user);

    @DELETE
    @Path("/{userId}")
    @Operation(summary = "Delete an existing user.")
    @APIResponse(responseCode = "204", description = "The user was deleted successfully")
    @APIResponse(responseCode = "404", description = "The user does not exist")
    void deleteUser(
            @Parameter(description = "ID of the user", required = true) @PathParam("userId") String userId);

}