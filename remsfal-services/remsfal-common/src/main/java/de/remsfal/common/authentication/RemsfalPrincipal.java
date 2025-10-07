package de.remsfal.common.authentication;

import java.security.Principal;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import de.remsfal.core.model.UserModel;

@RequestScoped
public class RemsfalPrincipal implements Principal, UserModel {

    @Inject
    JsonWebToken jwt;

    @Override
    public UUID getId() {
        return jwt != null && jwt.getSubject() != null ? UUID.fromString(jwt.getSubject()) : null;
    }

    @Override
    public String getEmail() {
        return jwt != null ? jwt.getClaim("email") : null;
    }

    @Override
    public String getName() {
        return jwt != null ? jwt.getClaim("name") : null;
    }

    @Override
    public Boolean isActive() {
        return jwt != null ? jwt.getClaim("active") : null;
    }

    public JsonWebToken getJwt() {
        return jwt;
    }

}
