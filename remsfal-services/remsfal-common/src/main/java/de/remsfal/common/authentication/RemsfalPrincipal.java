package de.remsfal.common.authentication;

import java.security.Principal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

import org.eclipse.microprofile.jwt.JsonWebToken;
import de.remsfal.core.model.UserModel;
import de.remsfal.core.model.ProjectMemberModel.MemberRole;

@RequestScoped
public class RemsfalPrincipal implements Principal, UserModel {

    @Inject
    JsonWebToken jwt;

    @Override
    public UUID getId() {
        return jwt != null && jwt.getSubject() != null ? UUID.fromString(jwt.getSubject()) : null;
    }

    /**
     * Returns the raw JWT subject.
     *
     * Important:
     * - JWT subject is a STRING by specification
     * - Not all services interpret it as UUID
     * - Ticketing Inbox uses string-based user ids (e.g. "user-owner")
     */
    public String getSubject() {
        return jwt != null ? jwt.getSubject() : null;
    }

    @Override
    public String getEmail() {
        return jwt != null ? jwt.getClaim("email") : null;
    }

    @Override
    public String getName() {
        return jwt != null ? jwt.getClaim("name") : null;
    }

    @Override
    public Boolean isActive() {
        return jwt != null ? jwt.getClaim("active") : null;
    }

    public JsonWebToken getJwt() {
        return jwt;
    }

    public Map<UUID, MemberRole> getProjectRoles() {
        return getClaimMap("project_roles").entrySet().stream()
            .collect(Collectors.toMap(
                e -> UUID.fromString(e.getKey()),
                e -> MemberRole.valueOf(e.getValue().trim())));
    }

    public Map<UUID, UUID> getTenancyProjects() {
        return getClaimMap("tenancy_projects").entrySet().stream()
            .collect(Collectors.toMap(
                e -> UUID.fromString(e.getKey()),
                e -> UUID.fromString(e.getValue())));
    }

    public Map<String, String> getClaimMap(String claimName) {
        if (jwt == null)
            return Collections.emptyMap();

        Object claim = jwt.getClaim(claimName);
        if (claim == null)
            return Collections.emptyMap();

        if (claim instanceof Map<?, ?> map) {
            Map<String, String> result = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    String value = entry.getValue().toString();
                    // Remove surrounding quotes if present
                    if (value.length() > 1 && value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    result.put(entry.getKey().toString(), value);
                }
            }
            return Map.copyOf(result); // immutable and safe
        } else if (claim instanceof JsonObject json) {
            Map<String, String> result = new LinkedHashMap<>();
            for (String key : json.keySet()) {
                result.put(key, json.getString(key));
            }
            return Map.copyOf(result);
        }
        return Collections.emptyMap();
    }

}