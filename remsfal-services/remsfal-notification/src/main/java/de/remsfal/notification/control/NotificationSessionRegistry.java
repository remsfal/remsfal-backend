package de.remsfal.notification.control;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.SseEventSink;

import org.jboss.logging.Logger;
import jakarta.inject.Inject;

import de.remsfal.core.json.eventing.IssueEventJson;

@ApplicationScoped
public class NotificationSessionRegistry {

    record Connection(UUID userId, Set<UUID> projectIds, Set<UUID> tenancyIds, SseEventSink sink) {}

    private final Map<SseEventSink, Connection> connections = new ConcurrentHashMap<>();

    @Inject
    Logger logger;

    public void register(UUID userId, Set<UUID> projectIds, Set<UUID> tenancyIds, SseEventSink sink) {
        connections.put(sink, new Connection(userId, projectIds, tenancyIds, sink));
        logger.infov("SSE registered (userId={0}, projects={1}, tenancies={2}, total={3})",
                userId, projectIds.size(), tenancyIds.size(), connections.size());
    }

    public void unregister(SseEventSink sink) {
        Connection removed = connections.remove(sink);
        if (removed != null) {
            logger.infov("SSE unregistered (userId={0}, total={1})", removed.userId(), connections.size());
        }
    }

    public void broadcast(IssueEventJson event, OutboundSseEvent outbound) {
        if (event == null || outbound == null) return;

        UUID projectId = event.getProjectId();
        UUID tenancyId = event.getTenancyId();
        IssueEventJson.Audience audience = event.getEffectiveAudience();

        UUID actorId = (event.getUser() != null) ? event.getUser().getId() : null;

        connections.values().forEach(conn -> {
            boolean isActor = actorId != null && actorId.equals(conn.userId());

            boolean shouldReceive = switch (audience) {
                case TENANCY_ALL ->
                        (tenancyId != null && conn.tenancyIds().contains(tenancyId)) || isActor;

                case PROJECT_ALL ->
                        (projectId != null && conn.projectIds().contains(projectId)) || isActor;

                case USER_ONLY -> {
                    UUID target = resolveTargetUserId(event);
                    yield (target != null && target.equals(conn.userId())) || isActor; // actor als fallback
                }
            };

            if (!shouldReceive) return;

            try {
                conn.sink().send(outbound);
            } catch (Exception e) {
                cleanup(conn.sink());
            }
        });
    }

    private UUID resolveTargetUserId(IssueEventJson event) {
        return switch (event.getIssueEventType()) {
            case ISSUE_ASSIGNED -> event.getOwnerId();
            case ISSUE_MENTIONED -> event.getMentionedUser() != null ? event.getMentionedUser().getId() : null;
            case ISSUE_CREATED, ISSUE_UPDATED -> null;
        };
    }

    private void cleanup(SseEventSink sink) {
        try { sink.close(); } catch (Exception ignored) {}
        unregister(sink);
    }
}