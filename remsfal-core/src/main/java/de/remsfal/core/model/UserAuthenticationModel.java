package de.remsfal.core.model;

import java.util.UUID;

public interface UserAuthenticationModel extends UserModel {

    UUID getRefreshTokenId();

}
