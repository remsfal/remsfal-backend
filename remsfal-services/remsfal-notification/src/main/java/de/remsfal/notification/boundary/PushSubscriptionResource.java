package de.remsfal.notification.boundary;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.api.notification.PushSubscriptionEndpoint;
import de.remsfal.core.json.notification.PushSubscriptionJson;
import de.remsfal.notification.control.PushController;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@Authenticated
public class PushSubscriptionResource implements PushSubscriptionEndpoint {

    @Inject
    RemsfalPrincipal principal;

    @Inject
    PushController pushController;

    @Override
    public Response registerFid(final PushSubscriptionJson subscription) {
        pushController.registerFid(principal.getId(), subscription.getFid(), subscription.getPlatform());
        return Response.status(Response.Status.CREATED).build();
    }

    @Override
    public Response sendTestPush() {
        pushController.sendToUser(principal.getId(), "REMSFAL", "Hallo von REMSFAL");
        return Response.noContent().build();
    }

    @Override
    public Response unregisterFid(final String fid){
        pushController.unregisterFid(principal.getId(), fid);
        return Response.noContent().build();
    }

}
