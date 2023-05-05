package de.remsfal.service.boundary.authentication;

import javax.enterprise.context.ApplicationScoped;

import de.remsfal.core.dto.ImmutableUserJson;
import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [stanik@htw-berlin.de]
 */
@ApplicationScoped
public class TokenValidator {

    public TokenInfo validate(final String authorizationHeader) {
        final UserModel dummyUser = ImmutableUserJson.builder()
            .id("8af38cd0-5aac-4d14-b3e6-341d0492836e")
            .email("dummy@example.org")
            .name("Max Mustermann")
            .build();
        return new TokenInfo(dummyUser);
    }

}
