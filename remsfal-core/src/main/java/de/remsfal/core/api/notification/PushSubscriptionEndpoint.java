package de.remsfal.core.api.notification;

import de.remsfal.core.json.notification.PushSubscriptionJson;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("/notification/v1/push-subscriptions")
public interface PushSubscriptionEndpoint {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Registriert die FID des aktuellen Geräts für Push-Benachrichtigungen.")
    @APIResponse(responseCode = "201", description = "FID registriert")
    @APIResponse(responseCode = "400", description = "Ungültige Anfrage")
    Response registerFid(@Valid PushSubscriptionJson subscription);

    @POST
    @Path("/test")
    @Operation(summary = "Send a test push notification to all devices of the current user")
    @APIResponse(responseCode = "204", description = "Test push was sent")
    @APIResponse(responseCode = "401", description = "Not authenticated")
    Response sendTestPush();

    @DELETE
    @Path("/{fid}")
    @Operation(summary = "Removes the push subscription of the current user for the given FID")
    @APIResponse(responseCode = "204", description = "Subscription removed")
    @APIResponse(responseCode = "401", description = "Not authenticated")
    Response unregisterFid(@PathParam("fid") String fid);
    
}
