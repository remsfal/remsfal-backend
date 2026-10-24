package de.remsfal.service.boundary.authentication;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import de.remsfal.service.control.event.IdentityRevocationEvent;
import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;

/**
 * Revokes the authorization of an external identity at its identity provider once the transaction that
 * requested it (e.g. the deletion of the user) has been committed. A failed revocation is logged only.
 */
@ApplicationScoped
public class IdentityRevocationObserver {

    @Inject
    Logger logger;

    @Inject
    AppleAuthenticator appleAuthenticator;

    void onRevocation(@Observes(during = TransactionPhase.AFTER_SUCCESS) final IdentityRevocationEvent event) {
        if (event.getProvider() != IdentityProvider.APPLE) {
            logger.warnv("Revocation is not supported for provider {0}", event.getProvider());
            return;
        }
        try {
            appleAuthenticator.revoke(event.getRefreshToken(), event.getClientId());
            logger.infov("Revoked Apple token (subject={0})", event.getSubject());
        } catch (RuntimeException e) {
            logger.errorv(e, "Unable to revoke Apple token (subject={0})", event.getSubject());
        }
    }

}
