package de.remsfal.service.boundary.authentication;

public class RefreshToken {
    private final String userId;
    private final String refreshToken;

    public RefreshToken(String userId, String refreshToken) {
        this.userId = userId;
        this.refreshToken = refreshToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
