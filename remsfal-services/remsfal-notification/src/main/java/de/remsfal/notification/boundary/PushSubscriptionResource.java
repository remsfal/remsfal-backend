package de.remsfal.notification.boundary;

import de.remsfal.common.authentication.RemsfalPrincipal;
import de.remsfal.core.api.PushSubscriptionEndpoint;
import de.remsfal.core.json.PushSubscriptionJson;
import de.remsfal.notification.control.PushController;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Authenticated
public class PushSubscriptionResource implements PushSubscriptionEndpoint {

    @Inject
    RemsfalPrincipal principal;

    @Inject
    PushController pushController;

    @Inject
    PushSubscriptionRepository repository;

    public void registerFid(UUID userId, String token, String platform) {
        PushSubscriptionEntity entity = repository.findByToken(token)
                .orElseGet(PushSubscriptionEntity::new);
        entity.setUserId(userId);   // gleiche FID, neuer Nutzer: Zuordnung wechselt
        entity.setToken(token);
        entity.setPlatform(platform);
        repository.save(entity);
    }

    public void sendToUser(UUID userId, String title, String body) {
        for (PushSubscriptionEntity sub : repository.findByUserId(userId)) {
            if (producer.send(sub.getToken(), title, body) == PushProducer.Result.UNREGISTERED) {
                repository.deleteByToken(sub.getToken());
            }
        }
    }


}
