package de.remsfal.notification.control;

import java.util.Map;
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

    public void registerPushSubscription(final UUID userId, final String token) {
        PushSubscriptionEntity entity = repository.findByToken(token)
                .orElseGet(PushSubscriptionEntity::new);
        entity.setUserId(userId);
        entity.setToken(token);
        repository.save(entity);
    }

    public void sendToUser(final UUID userId, final String title, final String body, final Map<String, String> data) {
        // ist für logger, falls kein Eintrag in db zu finden ist
        var subscriptions = repository.findByUserId(userId);
        if (subscriptions.isEmpty()) {
            LOG.warnf("No push subscription for user %s, nothing sent", userId);
        }
        for (PushSubscriptionEntity subscription : subscriptions) {
            PushProducer.Result result;
            result = producer.sendToken(subscription.getToken(), title, body, data);

            if (result == PushProducer.Result.UNREGISTERED) {
                LOG.infof("Removed unregistered push target %s", subscription.getToken());
                repository.deleteByToken(subscription.getToken());
            }
        }
    }

    public void deleteAllForUser(final UUID userId) {
        repository.deleteByUserId(userId);
    }

    public void unregisterPushSubscription(final UUID userId, final String token) {
        repository.findByToken(token)
                .filter(s -> userId.equals(s.getUserId()))
                .ifPresent(s -> repository.deleteByToken(token));
    }
}
