package de.remsfal.service.boundary.authentication;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TokenStore {

    private static TokenStore instance = null;
    private String jwt;

    private TokenStore() {
        jwt = "";
    }

    public static synchronized TokenStore getInstance() {
        if(instance == null) {
            instance = new TokenStore();
        }
        return instance;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}
