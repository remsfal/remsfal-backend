package de.remsfal.service.boundary.authentication;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;

import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [stanik@htw-berlin.de]
 */
public class TokenInfo implements UserModel {

    private final Payload payload;
    
    public TokenInfo(final Payload payload) {
        this.payload = payload;
    }

    @Override
    public String getId() {
        return payload.getSubject();
    }

    @Override
    public String getEmail() {
        return payload.getEmail();
    }

    @Override
    public String getName() {
        // Get profile information from payload
        // boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
        // String name = (String) payload.get("name");
        // String pictureUrl = (String) payload.get("picture");
        // String locale = (String) payload.get("locale");
        // String familyName = (String) payload.get("family_name");
        // String givenName = (String) payload.get("given_name");
        return (String) payload.get("name");
    }

}
