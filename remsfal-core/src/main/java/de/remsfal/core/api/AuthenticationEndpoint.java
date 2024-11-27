package de.remsfal.core.api;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

/**
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
@Path(AuthenticationEndpoint.CONTEXT + "/" + AuthenticationEndpoint.VERSION + "/" + AuthenticationEndpoint.SERVICE)
public interface AuthenticationEndpoint {

    String CONTEXT = "api";
    String VERSION = "v1";
    String SERVICE = "authentication";
    
    static boolean isAuthenticationPath(final String path) {
        final String basePath = "/" + AuthenticationEndpoint.CONTEXT + "/"
            + AuthenticationEndpoint.VERSION + "/" + AuthenticationEndpoint.SERVICE;
        final String loginPath = basePath + "/login";
        final String sessionPath = basePath + "/session";
        final String logoutPath = basePath + "/logout";
        return loginPath.equalsIgnoreCase(path)
            || sessionPath.equalsIgnoreCase(path)
            || logoutPath.equalsIgnoreCase(path);
    }

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

}
