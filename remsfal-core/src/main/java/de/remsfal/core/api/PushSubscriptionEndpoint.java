package de.remsfal.core.api;

import de.remsfal.core.json.PushSubscriptionJson;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
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
}
