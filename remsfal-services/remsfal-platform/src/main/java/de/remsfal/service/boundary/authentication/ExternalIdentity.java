package de.remsfal.service.boundary.authentication;

import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;

/**
 * A verified identity as asserted by an external identity provider.
 *
 * @param provider      the identity provider that asserted the identity
 * @param subject       the provider specific, stable subject identifier
 * @param email         the email address the provider reported (lower case)
 * @param emailVerified whether the provider has verified the email address
 */
public record ExternalIdentity(IdentityProvider provider, String subject, String email, boolean emailVerified) {
}
