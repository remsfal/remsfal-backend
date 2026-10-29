package de.remsfal.notification.control;

import java.util.UUID;

import de.remsfal.notification.entity.dao.PushSubscriptionRepository;
import de.remsfal.notification.entity.dto.PushSubscriptionEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PushController {

private static final Logger LOG = Logger.getLogger(PushController.class);

    @Inject
    PushProducer producer;

    @Inject
    PushSubscriptionRepository repository;

    public void registerFid(final UUID userId, final String fid, final String platform) {
        PushSubscriptionEntity entity = repository.findByFid(fid)
                .orElseGet(PushSubscriptionEntity::new);
        entity.setUserId(userId);
        entity.setFid(fid);
        entity.setPlatform(platform);
        repository.save(entity);
    }

    public void sendToUser(final UUID userId, final String title, final String body) {
        // var subscri... bis if (subscription.isEmpty)... ist für logger falls kein Eintrag in db ist
        var subscriptions = repository.findByUserId(userId);
        if (subscriptions.isEmpty()) {
            LOG.warnf("No push subscription for user %s, nothing sent", userId);
        }
        for (PushSubscriptionEntity subscription : subscriptions) {
            if (producer.send(subscription.getFid(), title, body) == PushProducer.Result.UNREGISTERED) {
                LOG.infof("Removed unregistered FID %s", subscription.getFid());
                repository.deleteByFid(subscription.getFid());
            }
        }
    }

    public void deleteAllForUser(final UUID userId) {
        repository.deleteByUserId(userId);
    }

    public void unregisterFid(final UUID userId, final String fid) {
        repository.findByFid(fid)
                .filter(s -> userId.equals(s.getUserId()))
                .ifPresent(s -> repository.deleteByFid(fid));
    }
}
