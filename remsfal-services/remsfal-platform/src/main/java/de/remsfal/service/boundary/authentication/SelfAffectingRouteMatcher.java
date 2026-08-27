package de.remsfal.service.boundary.authentication;

import jakarta.ws.rs.HttpMethod;

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

    private static final String TARGET_ID_GROUP = "targetId";

    private enum Route {
        ORGANIZATION_CREATE(HttpMethod.POST, "^/api/v1/organizations$", null),
        ORGANIZATION_DELETE(HttpMethod.DELETE, "^/api/v1/organizations/[^/]+$", null),
        ORGANIZATION_EMPLOYEE_UPDATE(HttpMethod.PATCH,
            "^/api/v1/organizations/[^/]+/employees/(?<" + TARGET_ID_GROUP + ">[^/]+)$", TARGET_ID_GROUP),
        ORGANIZATION_EMPLOYEE_DELETE(HttpMethod.DELETE,
            "^/api/v1/organizations/[^/]+/employees/(?<" + TARGET_ID_GROUP + ">[^/]+)$", TARGET_ID_GROUP),
        PROJECT_CREATE(HttpMethod.POST, "^/api/v1/projects$", null),
        PROJECT_DELETE(HttpMethod.DELETE, "^/api/v1/projects/[^/]+$", null),
        PROJECT_MEMBER_UPDATE(HttpMethod.PATCH,
            "^/api/v1/projects/[^/]+/members/(?<" + TARGET_ID_GROUP + ">[^/]+)$", TARGET_ID_GROUP),
        PROJECT_MEMBER_DELETE(HttpMethod.DELETE,
            "^/api/v1/projects/[^/]+/members/(?<" + TARGET_ID_GROUP + ">[^/]+)$", TARGET_ID_GROUP);

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
            Matcher matcher = route.method.equals(method) ? route.pattern.matcher(path) : null;
            if (matcher != null && matcher.matches()) {
                return isSelfAffecting(route, matcher, actorId);
            }
        }
        return false;
    }

    private static boolean isSelfAffecting(final Route route, final Matcher matcher, final UUID actorId) {
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

}
