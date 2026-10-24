package de.remsfal.service.boundary.authentication;

import java.net.URI;

import de.remsfal.service.entity.dto.UserIdentityEntity.IdentityProvider;

/**
 * An OpenID Connect identity provider using the authorization code flow.
 */
public interface OidcAuthenticator {

    IdentityProvider provider();

    /**
     * Builds the URI of the provider's authorization endpoint the user agent is redirected to.
     *
     * @param redirectUri callback URI the provider redirects back to
     * @param state       opaque state that is bound to the user agent
     */
    URI getAuthorizationCodeURI(String redirectUri, String state);

    /**
     * Exchanges the authorization code and verifies the returned ID token.
     *
     * @param code        authorization code from the callback
     * @param redirectUri callback URI that was used to obtain the code
     * @return the verified identity
     */
    ExternalIdentity authenticate(String code, URI redirectUri);

}
