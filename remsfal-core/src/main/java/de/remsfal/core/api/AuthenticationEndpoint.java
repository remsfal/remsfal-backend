package de.remsfal.core.api;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(AuthenticationEndpoint.CONTEXT + "/" + AuthenticationEndpoint.VERSION + "/" + AuthenticationEndpoint.SERVICE)
@PermitAll
public interface AuthenticationEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "authentication";

    @GET
    @Path("/login")
    @Operation(summary = "Login user via oauth flow.")
    @APIResponse(responseCode = "302", description = "Redirect user to the identity provider")
    Response login(@DefaultValue("/") @QueryParam("route") String route);

    @GET
    @Path("/session")
    @Operation(summary = "Start user session via oauth flow.")
    @APIResponse(responseCode = "302", description = "Redirect user to the frontend spa")
    Response session(@QueryParam("code") String code,
        @DefaultValue("/") @QueryParam("state") String state,
        @QueryParam("error") String error);

    @GET
    @Path("/logout")
    @Operation(summary = "Logout user identified by the session cookie.")
    @APIResponse(responseCode = "302", description = "Redirect user to the logout page")
    Response logout();

    @GET
    @Path("/jwks")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Expose the JSON Web Key Set used to sign tokens.")
    @APIResponse(responseCode = "200", description = "JWKS containing the public keys")
    Response jwks();

    @POST
    @Path("/refresh")
    @Operation(summary = "Refresh the access and refresh tokens using the refresh token cookie.")
    @APIResponse(responseCode = "204", description = "Tokens refreshed successfully, new tokens set as cookies")
    @APIResponse(responseCode = "401", description = "Unauthorized - Invalid or missing refresh token")
    Response refresh(@CookieParam("remsfal_refresh_token") Cookie refreshCookie);
}
