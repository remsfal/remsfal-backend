package de.remsfal.service.boundary.authentication;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Identifies requests that change the acting user's own authorization claims
 * (project/organization membership or role) and therefore require an immediate
 * access token renewal instead of waiting for the regular expiry-based renewal.
 *
 * @author Alexander Stanik [alexander.stanik@htw-berlin.de]
 */
public final class SelfAffectingRouteMatcher {

    private enum Route {
        ORGANIZATION_CREATE("POST", "^/api/v1/organizations$", null),
        ORGANIZATION_DELETE("DELETE", "^/api/v1/organizations/[^/]+$", null),
        ORGANIZATION_EMPLOYEE_UPDATE("PATCH",
            "^/api/v1/organizations/[^/]+/employees/(?<targetId>[^/]+)$", "targetId"),
        ORGANIZATION_EMPLOYEE_DELETE("DELETE",
            "^/api/v1/organizations/[^/]+/employees/(?<targetId>[^/]+)$", "targetId"),
        PROJECT_CREATE("POST", "^/api/v1/projects$", null),
        PROJECT_DELETE("DELETE", "^/api/v1/projects/[^/]+$", null),
        PROJECT_MEMBER_UPDATE("PATCH",
            "^/api/v1/projects/[^/]+/members/(?<targetId>[^/]+)$", "targetId"),
        PROJECT_MEMBER_DELETE("DELETE",
            "^/api/v1/projects/[^/]+/members/(?<targetId>[^/]+)$", "targetId");

        private final String method;
        private final Pattern pattern;
        private final String selfParam;

        Route(final String method, final String regex, final String selfParam) {
            this.method = method;
            this.pattern = Pattern.compile(regex);
            this.selfParam = selfParam;
        }
    }

    private SelfAffectingRouteMatcher() {
    }

    /**
     * Checks whether the given request mutates the acting user's own authorization claims.
     *
     * @param method   HTTP method of the request
     * @param path     request path, as returned by {@code UriInfo#getPath()}
     * @param actorId  user id of the requesting user, or null if unknown
     * @return true if the request is a self-affecting mutation requiring an immediate token renewal
     */
    public static boolean isForcedRenewalRequest(final String method, final String path, final UUID actorId) {
        for (Route route : Route.values()) {
            if (!route.method.equals(method)) {
                continue;
            }
            Matcher matcher = route.pattern.matcher(path);
            if (!matcher.matches()) {
                continue;
            }
            if (route.selfParam == null) {
                return true;
            }
            if (actorId == null) {
                return false;
            }
            try {
                return actorId.equals(UUID.fromString(matcher.group(route.selfParam)));
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return false;
    }

}
