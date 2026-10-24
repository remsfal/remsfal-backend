package de.remsfal.service.boundary.authentication;

import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;

/**
 * A verified identity as asserted by an external identity provider.
 *
 * @param provider      the identity provider that asserted the identity
 * @param subject       the provider specific, stable subject identifier
 * @param email         the email address the provider reported (lower case)
 * @param emailVerified whether the provider has verified the email address
 * @param firstName     the first name the provider reported, if any (Apple: first authorization only)
 * @param lastName      the last name the provider reported, if any (Apple: first authorization only)
 * @param refreshToken  the refresh token of the provider, if any (required to revoke the authorization)
 * @param clientId      the client ID the refresh token was issued to, if any
 */
public record ExternalIdentity(IdentityProvider provider, String subject, String email, boolean emailVerified,
    String firstName, String lastName, String refreshToken, String clientId) {

    public ExternalIdentity(final IdentityProvider provider, final String subject, final String email,
        final boolean emailVerified) {
        this(provider, subject, email, emailVerified, null, null, null, null);
    }

    public ExternalIdentity withName(final String firstName, final String lastName) {
        return new ExternalIdentity(provider, subject, email, emailVerified, firstName, lastName, refreshToken,
            clientId);
    }

}
