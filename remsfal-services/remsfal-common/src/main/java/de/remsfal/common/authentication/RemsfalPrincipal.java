package de.remsfal.common.authentication;

import java.security.Principal;

import jakarta.enterprise.context.RequestScoped;

import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [stanik@htw-berlin.de]
 */
@RequestScoped
public class RemsfalPrincipal implements Principal, UserModel {

    private UserModel user;
    private SessionInfo sessionInfo;

    public void setUserModel(final UserModel user) {
        this.user = user;
    }

    public void setSessionInfo(final SessionInfo sessionInfo) {
        this.sessionInfo = sessionInfo;
    }

    @Override
    public String getId() {
        if (sessionInfo != null) {
            return sessionInfo.getUserId();
        }
        return user != null ? user.getId() : null;
    }

    @Override
    public String getEmail() {
        if (sessionInfo != null) {
            return sessionInfo.getUserEmail();
        }
        return user != null ? user.getEmail() : null;
    }

    @Override
    public String getName() {
        return user != null ? user.getName() : null;
    }

    @Override
    public Boolean isActive() {
        return user != null ? user.isActive() : Boolean.TRUE;
    }

}
