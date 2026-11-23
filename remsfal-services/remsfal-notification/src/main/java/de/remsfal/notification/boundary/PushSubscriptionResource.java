package de.remsfal.notification.boundary;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.api.notification.PushSubscriptionEndpoint;
import de.remsfal.core.json.notification.PushSubscriptionJson;
import de.remsfal.notification.control.PushController;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Authenticated
public class PushSubscriptionResource implements PushSubscriptionEndpoint {

    @Inject
    RemsfalPrincipal principal;

    @Inject
    PushController pushController;

    @Override
    public Response registerPushSubscription(final PushSubscriptionJson subscription) {
        pushController.registerPushSubscription(principal.getId(), subscription.getToken());
        return Response.status(Response.Status.CREATED).build();
    }

    @Override
    public Response sendTestPush() {
        pushController.sendToUser(principal.getId(), "REMSFAL", "Hallo von REMSFAL", Map.of());
        return Response.noContent().build();
    }

    @Override
    public Response unregisterPushSubscription(final String token) {
        pushController.unregisterPushSubscription(principal.getId(), token);
        return Response.noContent().build();
    }

}
