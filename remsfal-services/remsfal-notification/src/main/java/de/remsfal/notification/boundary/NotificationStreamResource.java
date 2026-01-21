package de.remsfal.notification.boundary;

import java.util.Set;
import java.util.UUID;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.notification.control.NotificationSessionRegistry;

@Authenticated
@Path("/notification/stream")
public class NotificationStreamResource {

    @Inject
    RemsfalPrincipal principal;

    @Inject
    NotificationSessionRegistry registry;

    @Inject
    Sse sse;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void stream(@Context SseEventSink sink) {
        UUID userId = principal.getId();
        Set<UUID> projectIds = principal.getProjectRoles().keySet();
        Set<UUID> tenancyIds = principal.getTenancyProjects().keySet();

        registry.register(userId, projectIds, tenancyIds, sink);

        // initial event
        sink.send(
                sse.newEventBuilder()
                        .name("connected")
                        .data("ok")
                        .build()
        );

        // Optional: sofort raus wenn schon closed
        if (sink.isClosed()) {
            registry.unregister(sink);
        }
    }
}
