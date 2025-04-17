package de.remsfal.core.authentication;

import java.security.Principal;

import jakarta.enterprise.context.RequestScoped;

import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [stanik@htw-berlin.de]
 */
@RequestScoped
public class RemsfalPrincipal implements Principal, UserModel {

    private UserModel user;

    public void setUserModel(final UserModel user) {
        this.user = user;
    }

    @Override
    public String getId() {
        return user.getId();
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public Boolean isActive() {
        return user.isActive();
    }

}
