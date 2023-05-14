package de.remsfal.service.boundary.authentication;

import de.remsfal.core.model.UserModel;

/**
 * @author Alexander Stanik [stanik@htw-berlin.de]
 */
public class TokenInfo implements UserModel {

    final private UserModel user;
    
    public TokenInfo(final UserModel user) {
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

}
