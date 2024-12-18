package de.remsfal.core.model;

public interface UserAuthenticationModel {
    UserModel getUser();
    String getRefreshToken();
}
