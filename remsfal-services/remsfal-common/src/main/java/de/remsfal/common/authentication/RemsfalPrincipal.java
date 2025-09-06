package de.remsfal.common.authentication;

import java.security.Principal;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import de.remsfal.core.model.UserModel;

@RequestScoped
public class RemsfalPrincipal implements Principal, UserModel {

    // TODO: Once tokens always include `email`, `name`, and `active`, remove `fallbackUser`

    @Inject
    JsonWebToken jwt;

    /** Optional safety net until all services always include full profile claims */
    private UserModel fallbackUser;

    public void setUserModel(UserModel user) {
        this.fallbackUser = user;
    }

    /** User id from JWT `sub`; falls back to injected UserModel only if absent */
    @Override
    public String getId() {
        String sub = jwt != null ? jwt.getSubject() : null;
        if (sub != null && !sub.isBlank()) {
            return sub;
        }
        return fallbackUser != null ? fallbackUser.getId() : null;
    }

    /** Email from JWT `email`; falls back to injected UserModel only if absent */
    @Override
    public String getEmail() {
        String email = jwt != null ? jwt.getClaim("email") : null;
        if (email != null && !email.isBlank()) {
            return email;
        }
        return fallbackUser != null ? fallbackUser.getEmail() : null;
    }

    /** Display name from JWT `name`; falls back to injected UserModel only if absent */
    @Override
    public String getName() {
        String name = jwt != null ? jwt.getClaim("name") : null;
        if (name != null && !name.isBlank()) {
            return name;
        }
        return fallbackUser != null ? fallbackUser.getName() : null;
    }

    /** Active flag from JWT `active`; defaults to true; falls back to injected UserModel if missing */
    @Override
    public Boolean isActive() {
        Boolean active = jwt != null ? jwt.getClaim("active") : null;
        if (active != null) {
            return active;
        }
        if (fallbackUser != null && fallbackUser.isActive() != null) {
            return fallbackUser.isActive();
        }
        return Boolean.TRUE;
    }

    public JsonWebToken getJwt() {
        return jwt;
    }

}
