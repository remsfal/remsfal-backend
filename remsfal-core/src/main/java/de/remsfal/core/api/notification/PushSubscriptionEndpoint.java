package de.remsfal.core.api.notification;

import de.remsfal.core.json.notification.PushSubscriptionJson;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path(PushSubscriptionEndpoint.CONTEXT + "/" + PushSubscriptionEndpoint.VERSION + "/" +
        PushSubscriptionEndpoint.SERVICE)
public interface PushSubscriptionEndpoint {
    String CONTEXT = "notification";
    String VERSION = "v1";
    String SERVICE = "push-subscriptions";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Registriert die Firebase-Zielkennung des aktuellen Geräts für Push-Benachrichtigungen.")
    @APIResponse(responseCode = "201", description = "FID/FCM-Token registriert")
    @APIResponse(responseCode = "400", description = "Ungültige Anfrage")
    Response registerPushSubscription(@Valid PushSubscriptionJson subscription);

    @POST
    @Path("/test")
    @Operation(summary = "Send a test push notification to all devices of the current user")
    @APIResponse(responseCode = "204", description = "Test push was sent")
    @APIResponse(responseCode = "401", description = "Not authenticated")
    Response sendTestPush();

    @DELETE
    @Path("/{fidToken}")
    @Operation(summary = "Removes the push subscription of the current user for the given FID or Token")
    @APIResponse(responseCode = "204", description = "Subscription removed")
    @APIResponse(responseCode = "401", description = "Not authenticated")
    Response unregisterPushSubscription(@PathParam("fidToken") String fidToken);

}
