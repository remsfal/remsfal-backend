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

    public void registerPushSubscription(final UUID userId, final String fidToken, final String platform) {
        PushSubscriptionEntity entity = repository.findByFidToken(fidToken)
                .orElseGet(PushSubscriptionEntity::new);
        entity.setUserId(userId);
        entity.setFidToken(fidToken);
        entity.setPlatform(platform);
        repository.save(entity);
    }

    public void sendToUser(final UUID userId, final String title, final String body) {
        // ist für logger, falls kein Eintrag in db zu finden ist
        var subscriptions = repository.findByUserId(userId);
        if (subscriptions.isEmpty()) {
            LOG.warnf("No push subscription for user %s, nothing sent", userId);
        }
        for (PushSubscriptionEntity subscription : subscriptions) {
            PushProducer.Result result;

            if ("WEB".equals(subscription.getPlatform())) {
                result = producer.sendFid(subscription.getFidToken(), title, body);
            } else {
                result = producer.sendToken(subscription.getFidToken(), title, body);
            }

            if (result == PushProducer.Result.UNREGISTERED) {
                LOG.infof("Removed unregistered push target %s", subscription.getFidToken());
                repository.deleteByFidToken(subscription.getFidToken());
            }
        }
    }

    public void deleteAllForUser(final UUID userId) {
        repository.deleteByUserId(userId);
    }

    public void unregisterPushSubscription(final UUID userId, final String fidToken) {
        repository.findByFidToken(fidToken)
                .filter(s -> userId.equals(s.getUserId()))
                .ifPresent(s -> repository.deleteByFidToken(fidToken));
    }
}
