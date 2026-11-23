package de.remsfal.service.control.event;

import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;

/**
 * Fired when the authorization of an external identity has to be revoked at its identity provider, e.g. after the
 * user deleted the account.
 */
public record IdentityRevocationEvent(IdentityProvider provider, String subject, String refreshToken,
    String clientId) {
}
