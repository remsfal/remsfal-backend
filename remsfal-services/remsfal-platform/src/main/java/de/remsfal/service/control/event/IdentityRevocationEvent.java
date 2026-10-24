package de.remsfal.service.control.event;

import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;

/**
 * Fired when the authorization of an external identity has to be revoked at its identity provider, e.g. after the
 * user deleted the account.
 */
public class IdentityRevocationEvent {

    private final IdentityProvider provider;

    private final String subject;

    private final String refreshToken;

    private final String clientId;

    public IdentityRevocationEvent(final IdentityProvider provider, final String subject,
        final String refreshToken, final String clientId) {
        this.provider = provider;
        this.subject = subject;
        this.refreshToken = refreshToken;
        this.clientId = clientId;
    }

    public IdentityProvider getProvider() {
        return provider;
    }

    public String getSubject() {
        return subject;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getClientId() {
        return clientId;
    }

}
