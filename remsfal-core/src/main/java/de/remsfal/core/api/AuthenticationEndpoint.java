package de.remsfal.core.api;

import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
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

import de.remsfal.core.json.AppleNotificationJson;

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
    @Operation(summary = "Login user via oauth flow of the default identity provider (Google).",
        description = "Kept for backward compatibility, use the provider specific login endpoints instead.")
    @APIResponse(responseCode = "302", description = "Redirect user to the identity provider")
    Response login(@DefaultValue("/") @QueryParam("route") String route);

    @GET
    @Path("/login/google")
    @Operation(summary = "Login user via Google oauth flow.")
    @APIResponse(responseCode = "302", description = "Redirect user to Google")
    Response loginWithGoogle(@DefaultValue("/") @QueryParam("route") String route);

    @GET
    @Path("/login/apple")
    @Operation(summary = "Login user via Sign in with Apple oauth flow.")
    @APIResponse(responseCode = "302", description = "Redirect user to Apple")
    @APIResponse(responseCode = "503", description = "Sign in with Apple is not configured")
    Response loginWithApple(@DefaultValue("/") @QueryParam("route") String route);

    /**
     * @deprecated use {@link #googleSession(String, String, String)} instead
     */
    @Deprecated
    @GET
    @Path("/session")
    @Operation(summary = "Start user session via Google oauth flow (Google callback).",
        description = "Kept for backward compatibility, use /session/google instead.")
    @APIResponse(responseCode = "302", description = "Redirect user to the frontend spa")
    @APIResponse(responseCode = "401", description = "Authentication failed or state does not match")
    Response session(@QueryParam("code") String code,
        @QueryParam("state") String state,
        @QueryParam("error") String error);

    @GET
    @Path("/session/google")
    @Operation(summary = "Start user session via Google oauth flow (Google callback).")
    @APIResponse(responseCode = "302", description = "Redirect user to the frontend spa")
    @APIResponse(responseCode = "401", description = "Authentication failed or state does not match")
    Response googleSession(@QueryParam("code") String code,
        @QueryParam("state") String state,
        @QueryParam("error") String error);

    @POST
    @Path("/session/apple")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Start user session via Sign in with Apple oauth flow (Apple form_post callback).",
        description = "The user form field is only sent by Apple on the first authorization and carries the "
            + "name of the user as JSON.")
    @APIResponse(responseCode = "302", description = "Redirect user to the frontend spa")
    @APIResponse(responseCode = "401", description = "Authentication failed or state does not match")
    Response appleSession(@FormParam("code") String code,
        @FormParam("state") String state,
        @FormParam("user") String user,
        @FormParam("error") String error);

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(summary = "Login user via app token directly.",
        description = "For app_id=apple the app_token is the identity token of ASAuthorizationAppleIDProvider. "
            + "The raw nonce is required, the token must carry its SHA-256 hash. The authorization code and the "
            + "name are optional; Apple provides the name only on the first authorization.")
    @APIResponse(responseCode = "204", description = "Tokens are set as cookies")
    @APIResponse(responseCode = "401", description = "Unauthorized - Invalid token")
    @APIResponse(responseCode = "501", description = "The app is not supported")
    @APIResponse(responseCode = "503", description = "Sign in with Apple is not configured")
    Response token(@FormParam("app_id") @NotNull String appId,
        @FormParam("app_token") String appToken,
        @FormParam("dev_services") @DefaultValue("false") Boolean devService,
        @FormParam("authorization_code") String authorizationCode,
        @FormParam("nonce") String nonce,
        @FormParam("given_name") String givenName,
        @FormParam("family_name") String familyName);

    @POST
    @Path("/notifications/apple")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Receive server-to-server notifications of Sign in with Apple.",
        description = "Handles the events consent-revoked, account-delete, email-disabled and email-enabled.")
    @APIResponse(responseCode = "200", description = "Notification processed")
    @APIResponse(responseCode = "400", description = "Notification payload is invalid")
    Response appleNotification(@Valid @NotNull AppleNotificationJson notification);

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

    @GET
    @Path("/verify-additional-email")
    @Operation(summary = "Verify an additional user email address using a verification token.")
    @APIResponse(responseCode = "204", description = "Additional email verified successfully")
    @APIResponse(responseCode = "400", description = "Verification token is missing or expired")
    @APIResponse(responseCode = "404", description = "Verification token is invalid")
    Response verifyAdditionalEmail(@QueryParam("token") String token);
}
