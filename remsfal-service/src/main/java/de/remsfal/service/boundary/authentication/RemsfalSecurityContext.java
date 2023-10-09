package de.remsfal.service.boundary.authentication;

import java.security.Principal;

import jakarta.ws.rs.core.SecurityContext;

/**
 * @author Alexander Stanik [stanik@htw-berlin.de]
 */
public class RemsfalSecurityContext implements SecurityContext {
    
    public static final String BEARER = "Bearer";

    private final SecurityContext context;

    private final RemsfalPrincipal principal;

    private RemsfalSecurityContext(final SecurityContext context, final RemsfalPrincipal principal) {
        this.context = context;
        this.principal = principal;
    }

    public static RemsfalSecurityContext extendSecurityContext(final SecurityContext context, final RemsfalPrincipal principal) {
        return new RemsfalSecurityContext(context, principal);
    }
    
    public RemsfalPrincipal getPrincipal() {
        return principal;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(final String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return context.isSecure();
    }

    @Override
    public String getAuthenticationScheme() {
        return BEARER + "RemsfalSecurity";
    }

}
